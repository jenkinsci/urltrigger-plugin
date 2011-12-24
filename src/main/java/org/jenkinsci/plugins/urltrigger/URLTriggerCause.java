package org.jenkinsci.plugins.urltrigger;

import org.jenkinsci.lib.xtrigger.XTriggerCause;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerCause extends XTriggerCause {

    public URLTriggerCause(String causeFrom) {
        super("URLTrigger", causeFrom);
    }

}
