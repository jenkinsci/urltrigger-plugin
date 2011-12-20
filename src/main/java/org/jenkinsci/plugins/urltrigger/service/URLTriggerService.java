package org.jenkinsci.plugins.urltrigger.service;

import com.sun.jersey.api.client.ClientResponse;
import org.jenkinsci.plugins.urltrigger.URLTriggerEntry;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.URLTriggerLog;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;

import java.util.Date;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerService {

    private static URLTriggerService INSTANCE = new URLTriggerService();

    private URLTriggerService() {
    }

    public static URLTriggerService getInstance() {
        return INSTANCE;
    }

    //    public void refreshContent(ClientResponse clientResponse, URLTriggerEntry entry) throws URLTriggerException {
//        initContent(clientResponse, entry);
//    }
//
    public void initContent(ClientResponse clientResponse, URLTriggerEntry entry) throws URLTriggerException {

        if (clientResponse == null) {
            throw new NullPointerException("The given clientResponse object is not set.");
        }

        if (entry == null) {
            throw new NullPointerException("The given entry object is not set.");
        }

        Date lastModified = clientResponse.getLastModified();
        if (lastModified != null) {
            entry.setLastModificationDate(lastModified.getTime());
        } else {
            entry.setLastModificationDate(0);
        }

        if (entry.isInspectingContent()) {
            for (final URLTriggerContentType type : entry.getContentTypes()) {
                String stringContent = clientResponse.getEntity(String.class);
//                String stringContent = null;
//                try {
//                    InputStream entityInputStream = clientResponse.getEntityInputStream();
//                    if (entityInputStream != null) {
//                        stringContent = IOUtils.toString(entityInputStream);
//                    }
//                } catch (IOException ioe) {
//                    throw new URLTriggerException(ioe);
//                }
                if (stringContent == null) {
                    throw new URLTriggerException("The URL content is empty.");
                }
                type.initForContent(stringContent);
            }
        }
    }

    public boolean isSchedulingAndGetRefresh(ClientResponse clientResponse, URLTriggerEntry entry, URLTriggerLog log) throws URLTriggerException {
        //Check scheduling
        boolean job2Schedule = false;
        if (entry.isCheckStatus()) {
            job2Schedule = checkStatus(entry, log, clientResponse.getStatus());
        }
        if (entry.isCheckLastModificationDate()) {
            Date lastModificationDate = clientResponse.getLastModified();
            job2Schedule = job2Schedule || checkLastModificationDate(entry, log, lastModificationDate);
            refreshLatModificationDate(entry, lastModificationDate);
        }
        if (entry.isInspectingContent()) {
            String content = clientResponse.getEntity(String.class);
            job2Schedule = job2Schedule || checkContent(entry, log, content);
            refreshContent(entry, content);
        }

        return job2Schedule;
    }

    private void refreshLatModificationDate(URLTriggerEntry entry, Date lastModificationDate) {
        if (lastModificationDate != null) {
            entry.setLastModificationDate(lastModificationDate.getTime());
        } else {
            entry.setLastModificationDate(0);
        }
    }

    private void refreshContent(URLTriggerEntry entry, String content) throws URLTriggerException {

        for (final URLTriggerContentType type : entry.getContentTypes()) {
            //Refresh the content
            type.initForContent(content);
        }
    }

    private boolean checkStatus(URLTriggerEntry entry, URLTriggerLog log, int status) throws URLTriggerException {
        if (status == entry.getStatusCode()) {
            log.info(String.format("The returned status matches the expected status: \n %s", entry.getUrl()));
            return true;
        }
        return false;
    }

    private boolean checkLastModificationDate(URLTriggerEntry entry, URLTriggerLog log, Date clientLastModificationDate) throws URLTriggerException {

        boolean isTriggering = false;
        if (clientLastModificationDate != null) {
            long newLastModifiedDateTime = clientLastModificationDate.getTime();
            long previousLastModificationDateTime = entry.getLastModificationDate();
            if (previousLastModificationDateTime != 0 && previousLastModificationDateTime != newLastModifiedDateTime) {
                log.info("The last modification date has changed.");
                isTriggering = true;
            }
        }
        return isTriggering;
    }


    private boolean checkContent(URLTriggerEntry entry, URLTriggerLog log, String content) throws URLTriggerException {

        if (content == null) {
            return false;
        }

        log.info("Inspecting the content");
        for (final URLTriggerContentType type : entry.getContentTypes()) {
            boolean isTriggering = type.isTriggeringBuildForContent(content, log);
            if (isTriggering) {
                return true;
            }
        }

        return false;
    }

}
