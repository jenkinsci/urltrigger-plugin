package org.jenkinsci.plugins.urltrigger.service;

import java.util.Date;

/**
 * @author Victor Polozov
 */
public class FTPResponse implements URLResponse {

    private Date modifiedDate;
    private int status;
    private String content;
    private String etag;
    
    public FTPResponse(Date modifiedDate, int status) {
        this.modifiedDate = modifiedDate;
        this.status = status;
    }

    public FTPResponse() { }

    public Date getLastModified() {
        return modifiedDate;
    }
    
    public void setLastModified(Date lastModified) {
        this.modifiedDate = lastModified;
    }

    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }
    
    public void setStatus(int code) {
        status = code;
    }

    public String getEntityTagValue() {
        return etag;
    }
    
    public void setEntityTagValue(String etag) {
        this.etag = etag;
    }

    public boolean isSuccessfullFamily() {
        return status < 400; //http://en.wikipedia.org/wiki/List_of_FTP_server_return_codes
    }
    
}
