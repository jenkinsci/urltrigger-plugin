package org.jenkinsci.plugins.urltrigger;

import com.sun.jersey.api.client.ClientResponse;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.Secret;

import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
@SuppressWarnings("serial")
public class URLTriggerEntry implements Serializable , Describable< URLTriggerEntry> {

    public static final int DEFAULT_STATUS_CODE = ClientResponse.Status.OK.getStatusCode();

    private String url;
    private String username;
    private String password;
    private boolean proxyActivated;
    private boolean checkStatus;
    private int statusCode;
    private int timeout; //in seconds
    private boolean checkETag;
    private boolean checkLastModificationDate;
    private boolean inspectingContent;
    private boolean useGlobalEnvVars;
    private URLTriggerContentType[] contentTypes;
    private List<URLTriggerRequestHeader> requestHeaders = new ArrayList<URLTriggerRequestHeader>() ;

    private transient String ETag;
    private transient long lastModificationDate;

    public URLTriggerEntry() {
    }

    @DataBoundConstructor
    public URLTriggerEntry(String url, String username, String password, boolean proxyActivated, boolean checkStatus, int statusCode, int timeout, boolean checkETag, boolean checkLastModificationDate, boolean inspectingContent, URLTriggerContentType[] contentTypes, String ETag, long lastModificationDate) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.proxyActivated = proxyActivated;
        this.checkStatus = checkStatus;
        this.statusCode = statusCode;
        this.timeout = timeout;
        this.checkETag = checkETag;
        this.checkLastModificationDate = checkLastModificationDate;
        this.inspectingContent = inspectingContent;
        this.contentTypes = contentTypes;
        this.ETag = ETag;
        this.lastModificationDate = lastModificationDate;
    }

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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isFtp() {
        return url.startsWith("ftp");
    }

    public boolean isHttp() {
        return url.startsWith("http");
    }

    public boolean isHttps() {
        return url.startsWith("https");
    }

	public List<URLTriggerRequestHeader> getRequestHeaders() {
		return requestHeaders;
	}

	public void setRequestHeaders(List<URLTriggerRequestHeader> requestHeaders) {
		this.requestHeaders = new ArrayList<URLTriggerRequestHeader>(requestHeaders);
	}
	
    public Descriptor<URLTriggerEntry> getDescriptor() {
        return DESCRIPTOR;
    }

    public boolean isUseGlobalEnvVars() {
		return useGlobalEnvVars;
	}

	public void setUseGlobalEnvVars(boolean useGlobalEnvVars) {
		this.useGlobalEnvVars = useGlobalEnvVars;
	}

    @Extension
    public final static DescriptorImpl DESCRIPTOR = new DescriptorImpl();


    public static class DescriptorImpl extends Descriptor<URLTriggerEntry> {
        @Override
        public String getDisplayName() {
          return "UrlTriggerEntry";
        }
    }
}
