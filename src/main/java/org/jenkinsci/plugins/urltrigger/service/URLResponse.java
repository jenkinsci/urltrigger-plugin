package org.jenkinsci.plugins.urltrigger.service;

import java.util.Date;

/**
 * @author Victor Polozov
 */
public interface URLResponse {
    public Date getLastModified();
    public String getContent();
    public int getStatus();
    public String getEntityTagValue();
    public boolean isSuccessfullFamily();
}
