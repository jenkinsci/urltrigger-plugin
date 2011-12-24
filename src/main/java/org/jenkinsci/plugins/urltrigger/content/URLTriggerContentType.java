package org.jenkinsci.plugins.urltrigger.content;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;

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
    public abstract void initForContentType(String content) throws XTriggerException;


    public void initForContent(String content) throws XTriggerException {

        if (content == null) {
            throw new XTriggerException("The given content is not set.");
        }

        initForContentType(content);
    }

    public abstract boolean isTriggeringBuildForContent(String content, XTriggerLog log) throws XTriggerException;
}
