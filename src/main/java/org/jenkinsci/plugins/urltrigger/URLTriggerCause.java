package org.jenkinsci.plugins.urltrigger;

import hudson.model.Cause;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerCause extends Cause {

    public static final String NAME = "URLTrigger";
    public static final String CAUSE = "A change within the response URL invocation";

    @Override
    public String getShortDescription() {
        return String.format("[%s] - %s", NAME, CAUSE);
    }
}
