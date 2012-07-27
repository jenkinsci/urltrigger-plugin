package org.jenkinsci.plugins.urltrigger.service;

import com.sun.jersey.api.client.ClientResponse;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.urltrigger.URLTriggerEntry;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
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

    public void initContent(ClientResponse clientResponse, URLTriggerEntry entry, XTriggerLog log) throws XTriggerException {

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
                if (stringContent == null) {
                    throw new XTriggerException("The URL content is empty.");
                }
                type.initForContent(stringContent, log);
            }
        }
    }

    public boolean isSchedulingAndGetRefresh(ClientResponse clientResponse, URLTriggerEntry entry, XTriggerLog log) throws XTriggerException {

        boolean job2Schedule = false;

        if (entry.isCheckStatus()) {
            job2Schedule = checkStatus(entry, log, clientResponse.getStatus());
        }

        if (entry.isCheckETag()) {
            EntityTag entityTag = clientResponse.getEntityTag();
            if (entityTag != null) {
                job2Schedule = job2Schedule || checkEntityTag(entry, log, entityTag.getValue());
            }
            refreshETag(entry, entityTag == null ? null : entityTag.getValue());
        }

        if (entry.isCheckLastModificationDate()) {
            Date lastModificationDate = clientResponse.getLastModified();
            job2Schedule = job2Schedule || checkLastModificationDate(entry, log, lastModificationDate);
            refreshLatModificationDate(entry, lastModificationDate);
        }

        if (entry.isInspectingContent()) {

            //The response need to be in the successful family
            if (!isResponseInSuccessFamily(clientResponse)) {
                log.info("[WARNING] - Checking content requires success responses (200 status code family)");
                return false;
            }

            String content = clientResponse.getEntity(String.class);
            job2Schedule = job2Schedule || checkContent(entry, log, content);
            refreshContent(entry, content, log);
        }

        return job2Schedule;
    }

    private boolean isResponseInSuccessFamily(ClientResponse clientResponse) {
        return clientResponse.getClientResponseStatus().getFamily() == Response.Status.Family.SUCCESSFUL;
    }

    private void refreshETag(URLTriggerEntry entry, String entityTag) {
        entry.setETag(entityTag);
    }

    private void refreshLatModificationDate(URLTriggerEntry entry, Date lastModificationDate) {
        if (lastModificationDate != null) {
            entry.setLastModificationDate(lastModificationDate.getTime());
        } else {
            entry.setLastModificationDate(0);
        }
    }

    private void refreshContent(URLTriggerEntry entry, String content, XTriggerLog log) throws XTriggerException {
        URLTriggerContentType[] contentTypes = entry.getContentTypes();
        if (contentTypes != null) {
            for (final URLTriggerContentType type : contentTypes) {
                if (type != null) {
                    type.initForContent(content, log);
                }
            }
        }
    }

    private boolean checkStatus(URLTriggerEntry entry, XTriggerLog log, int status) throws XTriggerException {
        if (status == entry.getStatusCode()) {
            log.info(String.format("The returned status matches the expected status: \n %s", entry.getUrl()));
            return true;
        }
        return false;
    }

    private boolean checkEntityTag(URLTriggerEntry entry, XTriggerLog log, String entityTag) throws XTriggerException {

        boolean isTriggering = false;
        if (entityTag != null) {
            String previousETag = entry.getETag();
            if (!entityTag.equals(previousETag)) {
                log.info("The ETag header has changed.");
                isTriggering = true;
            }
        }
        return isTriggering;
    }

    private boolean checkLastModificationDate(URLTriggerEntry entry, XTriggerLog log, Date clientLastModificationDate) throws XTriggerException {

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

    private boolean checkContent(URLTriggerEntry entry, XTriggerLog log, String content) throws XTriggerException {

        if (content == null) {
            return false;
        }

        URLTriggerContentType[] contentTypes = entry.getContentTypes();
        if (contentTypes == null) {
            log.info("You have to add at least one content nature type to check.");
            return false;
        }

        log.info("Inspecting the content");
        for (final URLTriggerContentType type : contentTypes) {
            if (type != null) {
                boolean isTriggering = type.isTriggering(content, log);
                if (isTriggering) {
                    return true;
                }
            }
        }

        return false;
    }

}
