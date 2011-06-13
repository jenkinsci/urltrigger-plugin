package org.jenkinsci.plugins.urltrigger;

import hudson.model.Cause;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerCause extends Cause {

    @Override
    public String getShortDescription() {
        return "[URLTrigger] - Check the URL";
    }
}
