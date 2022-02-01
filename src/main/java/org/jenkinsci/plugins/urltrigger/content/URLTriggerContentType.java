package org.jenkinsci.plugins.urltrigger.content;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.jenkinsci.plugins.xtriggerapi.XTriggerLog;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public abstract class URLTriggerContentType implements ExtensionPoint, Describable<URLTriggerContentType>, Serializable {

    public Descriptor<URLTriggerContentType> getDescriptor() {
        return (URLTriggerContentTypeDescriptor<?>) Jenkins.get().getDescriptor(this.getClass());
    }

    public void initForContent(String content, XTriggerLog log) throws XTriggerException {

        if (content == null) {
            throw new XTriggerException("The given content is not set.");
        }

        if (content.trim().isEmpty()) {
            throw new XTriggerException("The given content is empty.");
        }

        initForContentType(content, log);
    }


    /**
     * These methods have to be overridden in each trigger implementation
     */
    protected abstract void initForContentType(String content, XTriggerLog log) throws XTriggerException;

    public boolean isTriggering(String content, XTriggerLog log) throws XTriggerException {
        return isTriggeringBuildForContent(content, log);
    }

    protected abstract boolean isTriggeringBuildForContent(String content, XTriggerLog log) throws XTriggerException;
}
