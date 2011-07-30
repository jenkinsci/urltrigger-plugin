package org.jenkinsci.plugins.urltrigger.content;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class TEXTContentEntry implements Serializable {

    private String regEx;

    @DataBoundConstructor
    public TEXTContentEntry(String regEx) {
        this.regEx = regEx;
    }

    public String getRegEx() {
        return regEx;
    }
}
