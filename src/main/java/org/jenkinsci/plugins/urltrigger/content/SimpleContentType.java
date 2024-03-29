package org.jenkinsci.plugins.urltrigger.content;

import hudson.Extension;
import hudson.Util;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.jenkinsci.plugins.xtriggerapi.XTriggerLog;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class SimpleContentType extends URLTriggerContentType {

	private static final long serialVersionUID = 6181433290922523995L;
	private transient String md5;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public SimpleContentType() {
    }

    @Override
    protected void initForContentType(String content, XTriggerLog log) throws XTriggerException {
        this.md5 = Util.getDigestOf(content);
    }

    @Override
    protected boolean isTriggeringBuildForContent(String content, XTriggerLog log) throws XTriggerException {

        if (md5 == null) {
            log.info("Capturing URL context. Waiting next schedule to check a change.");
            return false;
        }

        String newComputedMd5 = Util.getDigestOf(content);
        if (!newComputedMd5.equals(md5)) {
            log.info("The content of the URL has changed.");
            return true;
        }

        return false;
    }

    @Extension
    @SuppressWarnings("unused")
    @Symbol( "MD5Sum" )
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
