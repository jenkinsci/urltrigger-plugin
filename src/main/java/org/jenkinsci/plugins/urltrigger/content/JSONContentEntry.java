package org.jenkinsci.plugins.urltrigger.content;

import java.io.Serializable;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

/**
 * @author Gregory Boissinot
 */
public class JSONContentEntry implements Serializable , Describable<JSONContentEntry> {

	private static final long serialVersionUID = 3035792299750462535L;
	private String jsonPath;

    @DataBoundConstructor
    public JSONContentEntry(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    @SuppressWarnings("unused")
    public String getJsonPath() {
        return jsonPath;
    }

	@Extension
    @SuppressWarnings("unused")
    @Symbol( "JsonContentEntry" )
    public static class JSONContentEntryDescriptor extends Descriptor<JSONContentEntry> {
    }
    
    @Override
    public JSONContentEntryDescriptor getDescriptor() {
        return (JSONContentEntryDescriptor) Jenkins.get().getDescriptorOrDie(this.getClass());
    }

}
