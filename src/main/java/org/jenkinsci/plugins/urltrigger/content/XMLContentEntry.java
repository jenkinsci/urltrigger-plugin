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
public class XMLContentEntry implements Serializable, Describable<XMLContentEntry> {

    private static final long serialVersionUID = 1L;

    private String xPath;

    @DataBoundConstructor
    public XMLContentEntry(String xPath) {
        this.xPath = xPath;
    }

    @SuppressWarnings("unused")
    public String getXPath() {
        return xPath;
    }

    @Override
    public Descriptor<XMLContentEntry> getDescriptor() {
        return Jenkins.get().getDescriptorByType(XMLContentEntryDescriptor.class);
    }
    @Extension
    public static class XMLContentEntryDescriptor extends Descriptor<XMLContentEntry> {
        @Override
        public String getDisplayName() {
            return "XML Content Entry";
        }
    }
}
