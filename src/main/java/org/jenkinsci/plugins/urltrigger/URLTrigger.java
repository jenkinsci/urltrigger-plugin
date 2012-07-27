package org.jenkinsci.plugins.urltrigger;

import antlr.ANTLRException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.SequentialExecutionQueue;
import hudson.util.StreamTaskListener;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jenkinsci.lib.envinject.EnvInjectException;
import org.jenkinsci.lib.envinject.service.EnvVarsResolver;
import org.jenkinsci.lib.xtrigger.AbstractTrigger;
import org.jenkinsci.lib.xtrigger.XTriggerDescriptor;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentTypeDescriptor;
import org.jenkinsci.plugins.urltrigger.service.URLTriggerService;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Gregory Boissinot
 */
public class URLTrigger extends AbstractTrigger {

    private static Logger LOGGER = Logger.getLogger(URLTrigger.class.getName());

    private List<URLTriggerEntry> entries = new ArrayList<URLTriggerEntry>();

    private boolean labelRestriction;

    @DataBoundConstructor
    public URLTrigger(String cronTabSpec,
                      List<URLTriggerEntry> entries,
                      boolean labelRestriction,
                      String triggerLabel) throws ANTLRException {
        super(cronTabSpec, triggerLabel);
        this.entries = entries;
        this.labelRestriction = labelRestriction;
    }

    @SuppressWarnings("unused")
    public List<URLTriggerEntry> getEntries() {
        return entries;
    }

    @SuppressWarnings("unused")
    public boolean isLabelRestriction() {
        return labelRestriction;
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


    private String getURLValue(URLTriggerEntry entry, Node node) throws XTriggerException {
        String entryURL = entry.getUrl();
        if (entryURL != null) {
            EnvVarsResolver varsRetriever = new EnvVarsResolver();
            Map<String, String> envVars;
            try {
                envVars = varsRetriever.getPollingEnvVars((AbstractProject) job, node);
            } catch (EnvInjectException e) {
                throw new XTriggerException(e);
            }
            return Util.replaceMacro(entryURL, envVars);
        }
        return null;
    }

    @Override
    protected boolean checkIfModified(Node pollingNode, XTriggerLog log) throws XTriggerException {
        if (entries == null || entries.size() == 0) {
            log.info("No URLs to poll.");
            return false;
        }

        for (URLTriggerEntry entry : entries) {
            boolean modified = checkIfModifiedEntry(entry, pollingNode, log);
            if (modified) {
                return true;
            }
        }

        return false;
    }

    private boolean checkIfModifiedEntry(URLTriggerEntry entry, Node pollingNode, XTriggerLog log) throws XTriggerException {
        Client client = getClientObject(entry, log);
        String url = getURLValue(entry, pollingNode);
        log.info(String.format("Invoking the url: \n %s", url));

        ClientResponse clientResponse = client.resource(url).get(ClientResponse.class);
        if (isServiceUnavailableAndNotExpected(clientResponse, entry)) {
            log.info("URL to poll unavailable.");
            log.info("Skipping URLTrigger initialization. Waiting next schedule");
            return false;
        }

        URLTriggerService urlTriggerService = URLTriggerService.getInstance();
        if (urlTriggerService.isSchedulingAndGetRefresh(clientResponse, entry, log)) {
            return true;
        }

        return false;
    }

    private boolean isServiceUnavailableAndNotExpected(ClientResponse clientResponse, URLTriggerEntry entry) {
        return HttpServletResponse.SC_SERVICE_UNAVAILABLE == clientResponse.getStatus()
                && entry.getStatusCode() != HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    }

    @Override
    public String getCause() {
        return URLTriggerCause.CAUSE;
    }

    @Override
    protected String getName() {
        return URLTriggerCause.NAME;
    }

    private Client getClientObject(URLTriggerEntry entry, XTriggerLog log) {
        Client client = createClient(entry);
        if (isAuthBasic(entry)) {
            addBasicAuth(entry, log, client);
        }
        /* Set a connect and read timeout. If this hangs, it can actually
           take down all of the jenkins schedule events.
           This is 5 minutes expressed as milliseconds. */
        client.setConnectTimeout(300000);
        client.setReadTimeout(300000);
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

    private void addBasicAuth(URLTriggerEntry entry, XTriggerLog log, Client client) {
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

    @Override
    protected File getLogFile() {
        return new File(job.getRootDir(), "trigger-script-polling.log");
    }

    @Override
    protected Action[] getScheduledActions(Node node, XTriggerLog log) {
        return new Action[0];
    }

    @Override
    protected boolean requiresWorkspaceForPolling() {
        return false;
    }

    @Override
    protected void start(Node node, BuildableItem buildableItem, boolean newInstance, XTriggerLog log) {
        URLTriggerService service = URLTriggerService.getInstance();
        try {
            for (URLTriggerEntry entry : entries) {
                Client client = getClientObject(entry, null);
                String url = getURLValue(entry, null);
                ClientResponse clientResponse = client.resource(url).get(ClientResponse.class);
                if (HttpServletResponse.SC_SERVICE_UNAVAILABLE == clientResponse.getStatus()) {
                    log.info("URL to poll unavailable.");
                    log.info("Skipping URLTrigger initialization. Waiting for next schedule.");
                    return;
                }
                service.initContent(clientResponse, entry, new XTriggerLog((StreamTaskListener) TaskListener.NULL));
            }
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Severe error on trigger startup " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Override
    public URLTriggerDescriptor getDescriptor() {
        return (URLTriggerDescriptor) Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    @SuppressWarnings("unused")
    public static class URLTriggerDescriptor extends XTriggerDescriptor {

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
            boolean labelRestriction = false;
            String triggerLabel = null;
            Object labelRestrictionObject = formData.get("labelRestriction");
            if (labelRestrictionObject != null) {
                labelRestriction = true;
                triggerLabel = ((JSONObject) labelRestrictionObject).getString("triggerLabel");
            }

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
                urlTrigger = new URLTrigger(cronTabSpec, entries, labelRestriction, triggerLabel);
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

            //Process checkETag
            urlTriggerEntry.setCheckETag(entryObject.getBoolean("checkETag"));

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
                if (contentTypesJsonElt != null && !isEmptyJSONElement(contentTypesJsonElt)) {
                    List<URLTriggerContentType> types = req.bindJSONToList(URLTriggerContentType.class, contentTypesJsonElt);
                    urlTriggerEntry.setContentTypes(types.toArray(new URLTriggerContentType[types.size()]));
                } else {
                    urlTriggerEntry.setInspectingContent(false);
                }

            }

            return urlTriggerEntry;
        }

        private boolean isEmptyJSONElement(JSON element) {
            if (element instanceof JSONObject) {
                return ((JSONObject) element).getString("kind").isEmpty();
            }
            return false;
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
