package org.jenkinsci.plugins.urltrigger.content;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import jenkins.model.Jenkins;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public abstract class URLTriggerContentType implements ExtensionPoint, Describable<URLTriggerContentType>, Serializable {

    private static final long serialVersionUID = 1L;

    public Descriptor<URLTriggerContentType> getDescriptor() {
        return (URLTriggerContentTypeDescriptor) Jenkins.getActiveInstance().getDescriptor(getClass());
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
     * Initializes the trigger for a content type. Has to be overridden in each trigger implementation.
     *
     * @param content the content type
     * @param log the log for the trigger
     * @throws XTriggerException if there is any issue initializing the trigger
     */
    protected abstract void initForContentType(String content, XTriggerLog log) throws XTriggerException;

    public boolean isTriggering(String content, XTriggerLog log) throws XTriggerException {
        return isTriggeringBuildForContent(content, log);
    }

    protected abstract boolean isTriggeringBuildForContent(String content, XTriggerLog log) throws XTriggerException;
}
