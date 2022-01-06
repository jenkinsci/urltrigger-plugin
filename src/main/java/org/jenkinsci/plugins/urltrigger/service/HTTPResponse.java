package org.jenkinsci.plugins.urltrigger.service;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import java.util.Date;

/**
 * @author Victor Polozov
 */
public class HTTPResponse implements URLResponse {

    private Response baseResponse;

    public HTTPResponse(Response clientResponse) {
        baseResponse = clientResponse;
    }

    public Date getLastModified() {
        return baseResponse.getLastModified();
    }

    public String getContent() {
        return baseResponse.readEntity(String.class);
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
        return baseResponse.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
    }

}
