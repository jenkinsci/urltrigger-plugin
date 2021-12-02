package org.jenkinsci.plugins.urltrigger;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.lib.xtrigger.XTriggerCause;

import hudson.model.Cause;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerCause extends XTriggerCause implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "URLTrigger";
    public static final String CAUSE = "A change within the response URL invocation";
    private Map<String, String> triggerResponse;
    private String urlTrigger;

    protected URLTriggerCause() {
    	super(NAME , CAUSE) ;
    }

    protected URLTriggerCause( boolean logEnabled ) {
    	super(NAME , CAUSE , logEnabled) ;
    }

    protected URLTriggerCause(String triggerName, String causeFrom, boolean logEnabled) {
    	super(triggerName , causeFrom , logEnabled) ;
    }

    public void addTriggerResponse(Map<String, String> response) {
        if (triggerResponse != null) {
            triggerResponse.putAll(response);
        } else {
            setTriggerResponse(response);
        }
    }

    public void setTriggerResponse(Map<String, String> response) {
        triggerResponse = response;
    }

    public void setUrlTrigger(String url) {
        urlTrigger = url;
    }

    @Override
    public String getShortDescription() {
        return String.format("[%s] - %s of %s", NAME, CAUSE, urlTrigger);
    }

    public String getTriggerResponse() {
        List<String> result = new ArrayList<>();
        if (triggerResponse != null) {
            triggerResponse.forEach((key, value) -> result.add(String.format("\"%s\": %s", key, value)));
        }
        return StringUtils.joinWith(",", result);
    }

    public String getUrlTrigger() {
        return urlTrigger;
    }
}
