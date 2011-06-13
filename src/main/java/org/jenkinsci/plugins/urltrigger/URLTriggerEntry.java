package org.jenkinsci.plugins.urltrigger;

import com.sun.jersey.api.client.ClientResponse;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerEntry implements Serializable {

    public static final int DEFAULT_STATUS_CODE = ClientResponse.Status.OK.getStatusCode();

    private String url;

    private boolean checkStatus;

    private int statusCode;

    private boolean checkLastModifiedDate;

    private transient long lastModifiedDate;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(boolean checkStatus) {
        this.checkStatus = checkStatus;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isCheckLastModifiedDate() {
        return checkLastModifiedDate;
    }

    public void setCheckLastModifiedDate(boolean checkLastModifiedDate) {
        this.checkLastModifiedDate = checkLastModifiedDate;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
