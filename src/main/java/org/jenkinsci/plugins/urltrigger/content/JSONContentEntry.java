package org.jenkinsci.plugins.urltrigger.content;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class JSONContentEntry implements Serializable, Describable<JSONContentEntry> {

    private static final long serialVersionUID = 1L;

    private String jsonPath;

    @DataBoundConstructor
    public JSONContentEntry(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    @SuppressWarnings("unused")
    public String getJsonPath() {
        return jsonPath;
    }

    @Override
    public Descriptor<JSONContentEntry> getDescriptor() {
        return Jenkins.get().getDescriptorByType(JSONContentEntryDescriptor.class);
    }
    @Extension
    public static class JSONContentEntryDescriptor extends Descriptor<JSONContentEntry> {
        @Override
        public String getDisplayName() {
            return "JSON Content Entry";
        }
    }
}
