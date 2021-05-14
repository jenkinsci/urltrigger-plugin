package org.jenkinsci.plugins.urltrigger;

import java.io.Serializable;

import org.jenkinsci.lib.xtrigger.XTriggerCause;

import hudson.model.Cause;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerCauseTest extends URLTriggerCause {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    public URLTriggerCauseTest() {
    	super() ;
    }

    public URLTriggerCauseTest( boolean logEnabled ) {
    	super(logEnabled) ;
    }

    public URLTriggerCauseTest(String triggerName, String causeFrom, boolean logEnabled) {
    	super( triggerName , causeFrom , logEnabled ) ;
    }

}
