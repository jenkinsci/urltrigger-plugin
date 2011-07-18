package org.jenkinsci.plugins.urltrigger;

import com.sun.jersey.api.client.ClientResponse;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;

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

    private boolean inspectingContent;

    private URLTriggerContentType[] contentTypes;

    private transient long lastModificationDate;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isCheckStatus() {
        return checkStatus;
    }

    public boolean isInspectingContent() {
        return inspectingContent;
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

    public long getLastModificationDate() {
        return lastModificationDate;
    }

    public void setInspectingContent(boolean inspectingContent) {
        this.inspectingContent = inspectingContent;
    }

    public void setLastModificationDate(long lastModificationdDate) {
        this.lastModificationDate = lastModificationdDate;
    }

    public URLTriggerContentType[] getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(URLTriggerContentType[] contentTypes) {
        this.contentTypes = contentTypes;
    }
}
