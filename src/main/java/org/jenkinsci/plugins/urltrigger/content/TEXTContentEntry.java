package org.jenkinsci.plugins.urltrigger.content;

import hudson.Extension;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class TEXTContentEntry implements Serializable, Describable<TEXTContentEntry> {

    private static final long serialVersionUID = 1L;

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

    @Override
    public Descriptor<TEXTContentEntry> getDescriptor() {
        return Jenkins.get().getDescriptorByType(TEXTContentEntryDescriptor.class);
    }
    @Extension
    public static class TEXTContentEntryDescriptor extends Descriptor<TEXTContentEntry> {
        @Override
        public String getDisplayName() {
            return "TEXT Content Entry";
        }
    }
}
