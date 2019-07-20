package org.jenkinsci.plugins.urltrigger;

import com.sun.jersey.api.client.ClientResponse;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.Secret;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerEntry implements Serializable , Describable< URLTriggerEntry> {

	private static final long serialVersionUID = -7232627326475916056L;

	public static final int DEFAULT_STATUS_CODE = ClientResponse.Status.OK.getStatusCode();

    private String url;
    private String username;
    private String password;
    private boolean proxyActivated = false ;
    private boolean checkStatus = false ;
    private int statusCode;
    private int timeout; //in seconds
    private boolean checkETag = false ;
    private boolean checkLastModificationDate = false ;
    private boolean inspectingContent = false ;
    private boolean useGlobalEnvVars = false ;
    private URLTriggerContentType[] contentTypes = new URLTriggerContentType[0] ;
    private List<URLTriggerRequestHeader> requestHeaders = new ArrayList<URLTriggerRequestHeader>() ;

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")    
    private transient String ETag;
    
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient long lastModificationDate;

    public URLTriggerEntry() {
    }
    
    /**
     * Default data-bound constructor.
     * Given no other variables than a URL, we assume we're simply inspecting content
     * and monitoring for changes.
     * @param url
     */
    @DataBoundConstructor
    public URLTriggerEntry( String url ) {
    	this.url = url ;
    }
  
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
        this.contentTypes = contentTypes.clone();
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

    @DataBoundSetter
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

    @DataBoundSetter
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isProxyActivated() {
        return proxyActivated;
    }

    @DataBoundSetter
    public void setProxyActivated(boolean proxyActivated) {
        this.proxyActivated = proxyActivated;
    }

    public boolean isCheckStatus() {
        return checkStatus;
    }

    public boolean isInspectingContent() {
        return inspectingContent;
    }

    @DataBoundSetter
    public void setCheckStatus(boolean checkStatus) {
        this.checkStatus = checkStatus;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @DataBoundSetter
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode ;
        setCheckStatus( true ) ;
    }

    public boolean isCheckLastModificationDate() {
        return checkLastModificationDate;
    }

    @DataBoundSetter
    public void setCheckLastModificationDate(boolean checkLastModifiedDate) {
        this.checkLastModificationDate = checkLastModifiedDate;
    }

    public long getLastModificationDate() {
        return lastModificationDate;
    }

    @DataBoundSetter
    public void setInspectingContent(boolean inspectingContent) {
        this.inspectingContent = inspectingContent;
    }

    public void setLastModificationDate(long lastModificationdDate) {
        this.lastModificationDate = lastModificationdDate;
    }

    public URLTriggerContentType[] getContentTypes() {
        return contentTypes.clone();
    }

    @DataBoundSetter
    public void setContentTypes(URLTriggerContentType[] contentTypes) {
        this.contentTypes = contentTypes.clone() ;
        this.setInspectingContent( true ) ;
    }

    public boolean isCheckETag() {
        return checkETag;
    }

    @DataBoundSetter
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

    @DataBoundSetter
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
		if( requestHeaders == null ) {
			return new ArrayList<URLTriggerRequestHeader>() ;
		} else {
			return requestHeaders;
		}
	}

    @DataBoundSetter
	public void setRequestHeaders(List<URLTriggerRequestHeader> requestHeaders) {
		this.requestHeaders = new ArrayList<URLTriggerRequestHeader>(requestHeaders);
	}
	
    public Descriptor<URLTriggerEntry> getDescriptor() {
        return DESCRIPTOR;
    }

    public boolean isUseGlobalEnvVars() {
		return useGlobalEnvVars;
	}

    @DataBoundSetter
	public void setUseGlobalEnvVars(boolean useGlobalEnvVars) {
		this.useGlobalEnvVars = useGlobalEnvVars;
	}

    @Extension
    public final static DescriptorImpl DESCRIPTOR = new DescriptorImpl();


    @Symbol( "URLTriggerEntry" )
    public static class DescriptorImpl extends Descriptor<URLTriggerEntry> {
        @Override
        public String getDisplayName() {
          return "UrlTriggerEntry";
        }
    }
}
