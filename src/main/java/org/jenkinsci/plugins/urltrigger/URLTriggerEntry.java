package org.jenkinsci.plugins.urltrigger;

import com.sun.jersey.api.client.ClientResponse;
import hudson.util.Secret;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerEntry implements Serializable {

    public static final int DEFAULT_STATUS_CODE = ClientResponse.Status.OK.getStatusCode();

    private String url;
    private String username;
    private String password;
    private boolean proxyActivated;
    private boolean checkStatus;
    private int statusCode;
    private boolean checkETag;
    private boolean checkLastModificationDate;
    private boolean inspectingContent;
    private URLTriggerContentType[] contentTypes;

    private transient String ETag;
    private transient long lastModificationDate;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @SuppressWarnings("unused")
    public String getRealPassword() {
        if (password == null) {
            return "";
        }

        if (password.length() == 0) {
            return "";
        }

        Secret secret = Secret.fromString(password);
        return Secret.toString(secret);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isProxyActivated() {
        return proxyActivated;
    }

    public void setProxyActivated(boolean proxyActivated) {
        this.proxyActivated = proxyActivated;
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

    public boolean isCheckLastModificationDate() {
        return checkLastModificationDate;
    }

    public void setCheckLastModificationDate(boolean checkLastModifiedDate) {
        this.checkLastModificationDate = checkLastModifiedDate;
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

    public boolean isCheckETag() {
        return checkETag;
    }

    public void setCheckETag(boolean checkETag) {
        this.checkETag = checkETag;
    }

    public String getETag() {
        return ETag;
    }

    public void setETag(String ETag) {
        this.ETag = ETag;
    }
}
