package org.jenkinsci.plugins.urltrigger.service;

import com.sun.jersey.api.client.ClientResponse;
import org.jenkinsci.plugins.urltrigger.URLTriggerEntry;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;

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

    public void processEntry(ClientResponse clientResponse, URLTriggerEntry entry) throws URLTriggerException {

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

}
