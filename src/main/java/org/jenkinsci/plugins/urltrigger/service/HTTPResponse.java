package org.jenkinsci.plugins.urltrigger.service;

import com.sun.jersey.api.client.ClientResponse;
import java.util.Date;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;

/**
 * @author Victor Polozov
 */
public class HTTPResponse implements URLResponse {

    private ClientResponse baseResponse;
    
    public HTTPResponse(ClientResponse clientResponse) {
        baseResponse = clientResponse;
    }
    
    public Date getLastModified() {
        return baseResponse.getLastModified();
    }

    public String getContent() {
        return baseResponse.getEntity(String.class);
    }

    public int getStatus() {
        return baseResponse.getStatus();
    }

    public String getEntityTagValue() {
        EntityTag entityTag = baseResponse.getEntityTag();
        if (entityTag == null) {
            return null;
        }        
        return entityTag.getValue();
    }

    public boolean isSuccessfullFamily() {
        return baseResponse.getClientResponseStatus().getFamily() == Response.Status.Family.SUCCESSFUL;
    }
    
}
