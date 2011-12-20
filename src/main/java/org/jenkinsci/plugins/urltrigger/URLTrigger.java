package org.jenkinsci.plugins.urltrigger;

import antlr.ANTLRException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import hudson.*;
import hudson.model.*;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.SequentialExecutionQueue;
import hudson.util.StreamTaskListener;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentTypeDescriptor;
import org.jenkinsci.plugins.urltrigger.service.URLTriggerEnvVarsResolver;
import org.jenkinsci.plugins.urltrigger.service.URLTriggerService;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Gregory Boissinot
 */
public class URLTrigger extends Trigger<BuildableItem> implements Serializable {

    private static Logger LOGGER = Logger.getLogger(URLTrigger.class.getName());

    private List<URLTriggerEntry> entries = new ArrayList<URLTriggerEntry>();

    @DataBoundConstructor
    public URLTrigger(String cronTabSpec, List<URLTriggerEntry> entries) throws ANTLRException {
        super(cronTabSpec);
        this.entries = entries;
    }

    @SuppressWarnings("unused")
    public List<URLTriggerEntry> getEntries() {
        return entries;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {

        Map<String, String> subActionTitles = null;
        for (URLTriggerEntry entry : entries) {
            String url = entry.getUrl();
            URLTriggerContentType[] urlTriggerContentTypes = entry.getContentTypes();
            if (entry.getContentTypes() != null) {
                subActionTitles = new HashMap<String, String>(urlTriggerContentTypes.length);
                for (URLTriggerContentType fsTriggerContentFileType : urlTriggerContentTypes) {
                    if (fsTriggerContentFileType != null) {
                        Descriptor<URLTriggerContentType> descriptor = fsTriggerContentFileType.getDescriptor();
                        if (descriptor instanceof URLTriggerContentTypeDescriptor) {
                            subActionTitles.put(url, ((URLTriggerContentTypeDescriptor) descriptor).getLabel());
                        }
                    }
                }
            }
        }
        URLTriggerAction action = new URLTriggerAction((AbstractProject) job, getLogFile(), this.getDescriptor().getDisplayName(), subActionTitles);
        return Collections.singleton(action);
    }

    private Node getLauncherNode(URLTriggerLog log) {
        AbstractProject p = (AbstractProject) job;
        Label label = p.getAssignedLabel();
        if (label == null) {
            log.info("Running on master.");
            return getLauncherNodeMaster();
        } else {
            log.info(String.format("Searching a node to run the polling for the label '%s'.", label));
            return getLauncherNodeSlave(p, label, log);
        }
    }

    private Node getLauncherNodeMaster() {
        Computer computer = Hudson.getInstance().toComputer();
        if (computer != null) {
            return computer.getNode();
        } else {
            return null;
        }
    }

    private Node getLauncherNodeSlave(AbstractProject project, Label label, URLTriggerLog log) {
        Node lastBuildOnNode = project.getLastBuiltOn();
        boolean isAPreviousBuildNode = lastBuildOnNode != null;

        Set<Node> nodes = label.getNodes();
        for (Node node : nodes) {
            if (node != null) {
                if (!isAPreviousBuildNode) {
                    FilePath nodePath = node.getRootPath();
                    if (nodePath != null) {
                        log.info(String.format("Running on %s.", node.getNodeName()));
                        return node;
                    }
                } else {
                    FilePath nodeRootPath = node.getRootPath();
                    if (nodeRootPath != null) {
                        if (nodeRootPath.equals(lastBuildOnNode.getRootPath())) {
                            log.info("Running on " + node.getNodeName());
                            return lastBuildOnNode;
                        }
                    }
                }
            }
        }
        return null;
    }


    private String getURLValue(URLTriggerEntry entry, Node node, URLTriggerLog log) throws URLTriggerException {
        String entryURL = entry.getUrl();
        if (entryURL != null) {
            URLTriggerEnvVarsResolver resolver = new URLTriggerEnvVarsResolver();
            Map<String, String> envVars = resolver.getEnvVars((AbstractProject) job, node, log);
            return Util.replaceMacro(entryURL, envVars);
        }
        return null;
    }

    private boolean checkForScheduling(URLTriggerLog log) throws URLTriggerException {

        if (entries == null || entries.size() == 0) {
            log.info("No URLs to poll.");
            return false;
        }

        Node executionNode = getLauncherNode(log);
        for (URLTriggerEntry entry : entries) {
            Client client = getClientObject(entry, log);
            String url = getURLValue(entry, executionNode, log);
            log.info(String.format("Invoking the url: \n %s", url));
            ClientResponse clientResponse = client.resource(url).get(ClientResponse.class);
            URLTriggerService urlTriggerService = URLTriggerService.getInstance();
            if (urlTriggerService.isSchedulingAndGetRefresh(clientResponse, entry, log)) {
                return true;
            }
        }
        return false;
    }

    private Client getClientObject(URLTriggerEntry entry, URLTriggerLog log) {
        Client client = createClient(entry);
        if (isAuthBasic(entry)) {
            addBasicAuth(entry, log, client);
        }
        return client;
    }

    private Client createClient(URLTriggerEntry entry) {
        Client client;
        if (entry.isProxyActivated()) {
            client = createClientWithProxy();
        } else {
            client = createClientWithoutProxy();
        }
        return client;
    }

    private void addBasicAuth(URLTriggerEntry entry, URLTriggerLog log, Client client) {
        if (log != null) {
            log.info(String.format("Using Basic Authentication with the user '%s'", entry.getUsername()));
        }
        String password = entry.getRealPassword();
        client.addFilter(new HTTPBasicAuthFilter(entry.getUsername(), password));
    }

    private Client createClientWithoutProxy() {
        Client client;
        ClientConfig cc = new DefaultClientConfig();
        client = Client.create(cc);
        return client;
    }

    private Client createClientWithProxy() {
        Client client;
        DefaultApacheHttpClientConfig cc = new DefaultApacheHttpClientConfig();
        Hudson h = Hudson.getInstance(); // this code might run on slaves
        ProxyConfiguration p = h != null ? h.proxy : null;
        if (p != null) {
            cc.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI, "http://" + p.name + ":" + p.port);
            String password = getProxyPasswordDecrypted(p);
            cc.getState().setProxyCredentials(AuthScope.ANY_REALM, p.name, p.port, p.getUserName(), Util.fixNull(password));
        }
        client = ApacheHttpClient.create(cc);
        return client;
    }

    private String getProxyPasswordDecrypted(ProxyConfiguration p) {
        String passwordEncrypted = p.getPassword();
        String password = null;
        if (passwordEncrypted != null) {
            Secret secret = Secret.fromString(passwordEncrypted);
            password = Secret.toString(secret);
        }
        return password;
    }

    private boolean isAuthBasic(URLTriggerEntry entry) {
        return entry.getUsername() != null;
    }

    /**
     * Asynchronous task
     */
    protected class Runner implements Runnable, Serializable {

        private AbstractProject project;

        private URLTriggerLog log;

        Runner(AbstractProject project, URLTriggerLog log) {
            this.project = project;
            this.log = log;
        }

        public void run() {

            try {
                long start = System.currentTimeMillis();
                log.info("Polling started on " + DateFormat.getDateTimeInstance().format(new Date(start)));
                boolean scheduling = checkForScheduling(log);
                log.info("Polling complete. Took " + Util.getTimeSpanString(System.currentTimeMillis() - start));
                if (scheduling) {
                    log.info("There are changes. Scheduling a build.");
                    project.scheduleBuild(new URLTriggerCause());
                } else {
                    log.info("No changes.");
                }
            } catch (URLTriggerException e) {
                log.error("Polling error " + e.getMessage());
            } catch (Throwable e) {
                log.error("SEVERE - Polling error " + e.getMessage());
            }
        }
    }

    /**
     * Gets the triggering log file
     *
     * @return the trigger log
     */
    private File getLogFile() {
        return new File(job.getRootDir(), "trigger-script-polling.log");
    }

    @Override
    public void start(BuildableItem project, boolean newInstance) {
        super.start(project, newInstance);
        URLTriggerService service = URLTriggerService.getInstance();
        try {
            for (URLTriggerEntry entry : entries) {
                Client client = getClientObject(entry, null);
                String url = entry.getUrl();
                ClientResponse clientResponse = client.resource(url).get(ClientResponse.class);
                service.initContent(clientResponse, entry);
            }
        } catch (URLTriggerException urle) {
            LOGGER.log(Level.SEVERE, "Error on trigger startup " + urle.getMessage());
            urle.printStackTrace();
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Severe error on trigger startup " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (!Hudson.getInstance().isQuietingDown() && ((AbstractProject) this.job).isBuildable()) {
            URLScriptTriggerDescriptor descriptor = getDescriptor();
            ExecutorService executorService = descriptor.getExecutor();
            StreamTaskListener listener;
            try {
                listener = new StreamTaskListener(getLogFile());
                URLTriggerLog log = new URLTriggerLog(listener);
                if (job instanceof AbstractProject) {
                    Runner runner = new Runner((AbstractProject) job, log);
                    executorService.execute(runner);
                }

            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Severe Error during the trigger execution " + t.getMessage());
                t.printStackTrace();
            }
        }
    }


    @Override
    public URLScriptTriggerDescriptor getDescriptor() {
        return (URLScriptTriggerDescriptor) Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    @SuppressWarnings("unused")
    public static class URLScriptTriggerDescriptor extends TriggerDescriptor {

        private transient final SequentialExecutionQueue queue = new SequentialExecutionQueue(Executors.newSingleThreadExecutor());

        public ExecutorService getExecutor() {
            return queue.getExecutors();
        }

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public URLTrigger newInstance(StaplerRequest req, JSONObject formData) throws FormException {

            String cronTabSpec = formData.getString("cronTabSpec");
            Object entryObject = formData.get("urlElements");

            List<URLTriggerEntry> entries = new ArrayList<URLTriggerEntry>();
            if (entryObject instanceof JSONObject) {
                entries.add(fillAndGetEntry(req, (JSONObject) entryObject));
            } else {
                JSONArray jsonArray = (JSONArray) entryObject;
                if (jsonArray != null) {
                    for (Object aJsonArray : jsonArray) {
                        entries.add(fillAndGetEntry(req, (JSONObject) aJsonArray));
                    }
                }
            }

            URLTrigger urlTrigger;
            try {
                urlTrigger = new URLTrigger(cronTabSpec, entries);
            } catch (ANTLRException e) {
                throw new RuntimeException(e.getMessage());
            }

            return urlTrigger;

        }

        private URLTriggerEntry fillAndGetEntry(StaplerRequest req, JSONObject entryObject) {
            URLTriggerEntry urlTriggerEntry = new URLTriggerEntry();
            urlTriggerEntry.setUrl(entryObject.getString("url"));
            urlTriggerEntry.setProxyActivated(entryObject.getBoolean("proxyActivated"));
            String username = Util.fixEmpty(entryObject.getString("username"));
            if (username != null) {
                urlTriggerEntry.setUsername(username);
                Secret secret = Secret.fromString(Util.fixEmpty(entryObject.getString("password")));
                String encryptedValue = secret.getEncryptedValue();
                urlTriggerEntry.setPassword(encryptedValue);
            }

            //Process checkStatus
            Object checkStatusObject = entryObject.get("checkStatus");
            if (checkStatusObject != null) {
                urlTriggerEntry.setCheckStatus(true);
                try {
                    JSONObject statusJSONObject = (JSONObject) checkStatusObject;
                    urlTriggerEntry.setStatusCode(statusJSONObject.getInt("statusCode"));
                } catch (JSONException jsne) {
                    urlTriggerEntry.setStatusCode(URLTriggerEntry.DEFAULT_STATUS_CODE);
                }
            } else {
                urlTriggerEntry.setCheckStatus(false);
                urlTriggerEntry.setStatusCode(URLTriggerEntry.DEFAULT_STATUS_CODE);
            }

            //Process checkLastModifiedDate
            urlTriggerEntry.setCheckLastModificationDate(entryObject.getBoolean("checkLastModificationDate"));

            //Process inspectingContent
            Object inspectingContentObject = entryObject.get("inspectingContent");
            if (inspectingContentObject == null) {
                urlTriggerEntry.setInspectingContent(false);
                urlTriggerEntry.setContentTypes(new URLTriggerContentType[0]);
            } else {
                urlTriggerEntry.setInspectingContent(true);
                JSONObject inspectingContentJSONObject = entryObject.getJSONObject("inspectingContent");
                JSON contentTypesJsonElt;
                try {
                    contentTypesJsonElt = inspectingContentJSONObject.getJSONArray("contentTypes");
                } catch (JSONException jsone) {
                    contentTypesJsonElt = inspectingContentJSONObject.getJSONObject("contentTypes");
                }
                List<URLTriggerContentType> types = req.bindJSONToList(URLTriggerContentType.class, contentTypesJsonElt);
                urlTriggerEntry.setContentTypes(types.toArray(new URLTriggerContentType[types.size()]));

            }

            return urlTriggerEntry;
        }

        @Override
        public String getDisplayName() {
            return Messages.urltrigger_displayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/urltrigger/help.html";
        }

        @SuppressWarnings("unchecked")
        public DescriptorExtensionList getListURLTriggerDescriptors() {
            return DescriptorExtensionList.createDescriptorList(Hudson.getInstance(), URLTriggerContentType.class);
        }

        public FormValidation doCheckStatus(@QueryParameter String value) {

            if (value == null || value.trim().isEmpty()) {
                return FormValidation.ok();
            }
            try {
                Integer.parseInt(value);
                return FormValidation.ok();
            } catch (Exception e) {
                return FormValidation.error("You must provide a valid number status such as 200, 301, ...");
            }
        }


        public FormValidation doCheckURL(@QueryParameter String value) {

            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("The url field is mandatory.");
            }
            try {
                ClientConfig cc = new DefaultClientConfig();
                Client client = Client.create(cc);
                client.resource(value).get(ClientResponse.class);
                return FormValidation.ok();
            } catch (Exception e) {
                return FormValidation.error(e.getMessage());
            }
        }


    }

}
