package org.jenkinsci.plugins.urltrigger;

import java.io.Serializable;

import org.jenkinsci.plugins.xtriggerapi.XTriggerCause;

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
    private String urlTrigger;

    protected URLTriggerCause() {
    	super(NAME , CAUSE) ;
    }

    protected URLTriggerCause( boolean logEnabled ) {
    	super(NAME , CAUSE , logEnabled) ;
    }

    protected URLTriggerCause(String triggerName, String causeFrom, boolean logEnabled) {
    	super( triggerName , causeFrom , logEnabled ) ;
    }

    public void setUrlTrigger(String url) {
        urlTrigger = url;
    }

    @Override
    public String getShortDescription() {
        return String.format("[%s] - %s of %s", NAME, CAUSE, urlTrigger);
    }
    
    public String getUrlTrigger() {
        return urlTrigger;
    }
}
