package org.jenkinsci.plugins.urltrigger.service;

import java.util.Date;

/**
 * @author Victor Polozov
 */
public interface URLResponse {
    Date getLastModified();

    String getContent();

    int getStatus();

    String getEntityTagValue();

    boolean isSuccessfullFamily();
}
