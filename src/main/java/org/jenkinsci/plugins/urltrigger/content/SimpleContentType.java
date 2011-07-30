package org.jenkinsci.plugins.urltrigger.content;

import hudson.Extension;
import hudson.Util;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.URLTriggerLog;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class SimpleContentType extends URLTriggerContentType {

    private transient String md5;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public SimpleContentType() {
    }

    @Override
    public void initForContentType(String content) throws URLTriggerException {
        this.md5 = Util.getDigestOf(content);
    }

    @Override
    public boolean isTriggeringBuildForContent(String content, URLTriggerLog log) throws URLTriggerException {
        assert md5 != null;

        String newComputedMd5 = Util.getDigestOf(content);
        if (!newComputedMd5.equals(md5)) {
            log.info("The content of the URL has changed.");
            return true;
        }

        return false;
    }

    @Extension
    @SuppressWarnings("unused")
    public static class SimpleFileContentDescriptor extends URLTriggerContentTypeDescriptor<SimpleContentType> {

        @Override
        public Class<? extends URLTriggerContentType> getType() {
            return SimpleContentType.class;
        }

        @Override
        public String getDisplayName() {
            return "Monitor a change of the content";
        }

        @Override
        public String getLabel() {
            return getDisplayName();
        }
    }
}
