package org.jenkinsci.plugins.urltrigger;

import java.io.Serial;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerCauseTest extends URLTriggerCause {

    @Serial
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
