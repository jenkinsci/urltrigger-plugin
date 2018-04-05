package org.jenkinsci.plugins.urltrigger ;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import hudson.model.Describable;
import hudson.model.Descriptor;


/**
 * @author  Tony Noble
 */
public class URLTriggerRequestHeader implements Describable<URLTriggerRequestHeader>{
    @Exported public String headerName = "";
    @Exported public String headerValue = "";
    
    @DataBoundConstructor
    public URLTriggerRequestHeader(String headerName, String headerValue) {
      this.headerName=headerName;
      this.headerValue=headerValue;
    }
    
    public Descriptor<URLTriggerRequestHeader> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public final static DescriptorImpl DESCRIPTOR = new DescriptorImpl();


    public static class DescriptorImpl extends Descriptor<URLTriggerRequestHeader> {
        @Override
        public String getDisplayName() {
          return "UrlTriggerRequestHeader";
        }
    }
    
    
    public static URLTriggerRequestHeader[] getDefaults() {
    	return new URLTriggerRequestHeader[] { new URLTriggerRequestHeader( "" , "" ) } ;
    }
    
}