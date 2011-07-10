package org.jenkinsci.plugins.urltrigger;

import antlr.ANTLRException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.SequentialExecutionQueue;
import hudson.util.StreamTaskListener;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentTypeDescriptor;
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
                for (int i = 0; i < urlTriggerContentTypes.length; i++) {
                    URLTriggerContentType fsTriggerContentFileType = urlTriggerContentTypes[i];
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

    private boolean checkForScheduling(URLTriggerLog log) throws URLTriggerException {
        ClientConfig cc = new DefaultClientConfig();
        Client client = Client.create(cc);
        for (URLTriggerEntry entry : entries) {

            //Get the url
            String url = entry.getUrl();

            //Invoke the Url and process its response
            log.info(String.format("Invoking the url: \n %s", url));
            ClientResponse clientResponse = client.resource(url).get(ClientResponse.class);

            //Check the status if needed
            if (entry.isCheckStatus()) {
                int status = clientResponse.getStatus();
                if (status == entry.getStatusCode()) {
                    log.info(String.format("The returned status match the expected status: \n %s", url));
                    return true;
                }
            }

            //Check the last modified date if needed
            if (entry.isCheckLastModifiedDate()) {
                Date lastModifiedDate = clientResponse.getLastModified();
                if (lastModifiedDate != null) {
                    long newLastModifiedDate = lastModifiedDate.getTime();
                    if (entry.getLastModifiedDate() == 0L) {
                        entry.setLastModifiedDate(newLastModifiedDate);
                        return false;
                    }
                    if (entry.getLastModifiedDate() != newLastModifiedDate) {
                        entry.setLastModifiedDate(newLastModifiedDate);
                        log.info("The last modification date has changed.");
                        return true;
                    }
                }
            }

            //Check the url content
            //Call from master (it's an URL, it doesn't matter to call from a slave)
            if (entry.isInspectingContent()) {
                log.info("Inspecting the content");
                for (final URLTriggerContentType type : entry.getContentTypes()) {
                    String xmlString = clientResponse.getEntity(String.class);
                    if (xmlString == null) {
                        throw new URLTriggerException("The URL content is empty.");
                    }
                    boolean isTriggered = type.isTriggeringBuildForContent(xmlString, log);
                    if (isTriggered) {
                        return true;
                    }
                }
            }
        }
        return false;
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

        try {
            // Initialize the memory information if whe introspect the content
            initContentElementsIfNeed();

        } catch (URLTriggerException urle) {
            LOGGER.log(Level.SEVERE, "Error on trigger startup " + urle.getMessage());
            urle.printStackTrace();
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Severe error on trigger startup " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void initContentElementsIfNeed() throws URLTriggerException {
        ClientConfig cc = new DefaultClientConfig();
        Client client = Client.create(cc);
        for (URLTriggerEntry entry : entries) {

            //Get the url
            String url = entry.getUrl();

            //Invoke the Url and process its response
            ClientResponse clientResponse = client.resource(url).get(ClientResponse.class);

            entry.setLastModifiedDate(clientResponse.getLastModified().getTime());

            if (entry.isInspectingContent()) {
                for (final URLTriggerContentType type : entry.getContentTypes()) {
                    String xmlString = clientResponse.getEntity(String.class);
                    if (xmlString == null) {
                        throw new URLTriggerException("The URL content is empty.");
                    }
                    type.initForContent(xmlString);
                }
            }

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
                Iterator it = jsonArray.iterator();
                while (it.hasNext()) {
                    entries.add(fillAndGetEntry(req, (JSONObject) it.next()));
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
            Object checkLastModifiedDateObject = entryObject.get("checkLastModifiedDate");
            if (checkLastModifiedDateObject != null) {
                urlTriggerEntry.setCheckLastModifiedDate(true);
            } else {
                urlTriggerEntry.setCheckLastModifiedDate(false);
            }

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
            return "Poll with a URL";
        }

        @Override
        public String getHelpFile(){
            return "/plugin/urltrigger/help.html";
        }

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
