package org.jenkinsci.plugins.urltrigger.content;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.URLTriggerLog;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public abstract class URLTriggerContentType implements ExtensionPoint, Describable<URLTriggerContentType>, Serializable {

    public Descriptor<URLTriggerContentType> getDescriptor() {
        return (URLTriggerContentTypeDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    /**
     * These methods have to be overridden in each trigger implementation
     */
    public abstract void initForContentType(String content) throws URLTriggerException;


    public void initForContent(String content) throws URLTriggerException {

        if (content == null) {
            throw new URLTriggerException("The given content is not set.");
        }

        initForContentType(content);
    }

    public abstract boolean isTriggeringBuildForContent(String content, URLTriggerLog log) throws URLTriggerException;
}
