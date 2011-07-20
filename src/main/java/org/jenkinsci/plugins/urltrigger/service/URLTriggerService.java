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

    public void processURLEntryFromStartStage(ClientResponse clientResponse, URLTriggerEntry entry) throws URLTriggerException {

        entry.setLastModificationDate(clientResponse.getLastModified().getTime());

        if (entry.isInspectingContent()) {
            for (final URLTriggerContentType type : entry.getContentTypes()) {
                String stringContent = clientResponse.getEntity(String.class);
                if (stringContent == null) {
                    throw new URLTriggerException("The URL content is empty.");
                }
                type.initForContent(stringContent);
            }
        }

    }

    public boolean isSchedulingForURLEntry(ClientResponse clientResponse, URLTriggerEntry entry, URLTriggerLog log) throws URLTriggerException {
        //Get the url
        String url = entry.getUrl();

        //Check the status if needed
        if (entry.isCheckStatus()) {
            int status = clientResponse.getStatus();
            if (status == entry.getStatusCode()) {
                log.info(String.format("The returned status matches the expected status: \n %s", url));
                return true;
            }
        }

        //Check the last modified date if needed
        if (entry.isCheckLastModificationDate()) {
            Date lastModifiedDate = clientResponse.getLastModified();
            if (lastModifiedDate != null) {
                long newLastModifiedDate = lastModifiedDate.getTime();
                if (entry.getLastModificationDate() == 0L) {
                    entry.setLastModificationDate(newLastModifiedDate);
                    return false;
                }
                if (entry.getLastModificationDate() != newLastModifiedDate) {
                    entry.setLastModificationDate(newLastModifiedDate);
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

        return false;
    }

}
