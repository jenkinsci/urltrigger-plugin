package org.jenkinsci.plugins.urltrigger.content;

import hudson.Util;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class TEXTContentEntry implements Serializable {

    private String regEx;

    @DataBoundConstructor
    public TEXTContentEntry(String regEx) {
        this.regEx = Util.fixEmptyAndTrim(regEx);
        if (this.regEx == null) {
            this.regEx = ".*";
        }
    }

    public String getRegEx() {
        return regEx;
    }
}
