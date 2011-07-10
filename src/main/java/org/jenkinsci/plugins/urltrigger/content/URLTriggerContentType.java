package org.jenkinsci.plugins.urltrigger.content;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.URLTriggerLog;

import java.io.File;
import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public abstract class URLTriggerContentType implements ExtensionPoint, Describable<URLTriggerContentType>, Serializable {

    public Descriptor<URLTriggerContentType> getDescriptor() {
        return (URLTriggerContentTypeDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    /**
     * Cycle of the trigger
     * These methods have to be overridden in each trigger implementation
     */
    public abstract void initForContent(String content) throws URLTriggerException;
    public abstract boolean isTriggeringBuildForContent(String content, URLTriggerLog log) throws URLTriggerException;
}
