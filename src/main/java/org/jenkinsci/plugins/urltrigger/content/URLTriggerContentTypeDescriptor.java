package org.jenkinsci.plugins.urltrigger.content;

import hudson.model.Descriptor;

/**
 * @author Gregory Boissinot
 */
public abstract class URLTriggerContentTypeDescriptor<T extends URLTriggerContentType> extends Descriptor<URLTriggerContentType> {

    public abstract String getLabel();

    public abstract Class<? extends URLTriggerContentType> getType();

    @SuppressWarnings("unused")
    public String getTypePackageName() {
        return getType().getName();
    }
}
