package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class XMLContentEntry implements Serializable , Describable<XMLContentEntry> {

	private static final long serialVersionUID = 7748907388851034421L;
	private String xPath;

    @DataBoundConstructor
    public XMLContentEntry(String xPath) {
        this.xPath = xPath;
    }

    public String getXPath() {
        return xPath;
    }
    
	@Extension
    @Symbol( "XMLContentEntry" )
    public static class XMLContentEntryDescriptor extends Descriptor<XMLContentEntry> {
    }
    
    @Override
    public XMLContentEntryDescriptor getDescriptor() {
        return (XMLContentEntryDescriptor) Jenkins.get().getDescriptorOrDie(this.getClass());
    }

}
