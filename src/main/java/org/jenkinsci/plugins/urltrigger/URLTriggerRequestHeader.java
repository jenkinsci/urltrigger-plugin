package org.jenkinsci.plugins.urltrigger ;

import hudson.Extension;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.export.Exported;

import hudson.model.Describable;
import hudson.model.Descriptor;

import java.io.Serializable;

/**
 * @author  Tony Noble
 */
public class URLTriggerRequestHeader implements Serializable, Describable<URLTriggerRequestHeader>{ 
	
	private static final long serialVersionUID = -4013307449944349433L;
	@Exported public String headerName = "";
	@Exported public String headerValue = "";
    
    public String getHeaderName() {
		return headerName;
	}

    @DataBoundSetter
	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getHeaderValue() {
		return headerValue;
	}

    @DataBoundSetter
	public void setHeaderValue(String headerValue) {
		this.headerValue = headerValue;
	}

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

    @Symbol( "RequestHeader" )
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