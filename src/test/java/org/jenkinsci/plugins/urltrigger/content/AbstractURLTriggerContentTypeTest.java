package org.jenkinsci.plugins.urltrigger.content;

import hudson.util.StreamTaskListener;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractURLTriggerContentTypeTest {

    protected XTriggerLog log = new XTriggerLog((StreamTaskListener) hudson.model.TaskListener.NULL);

    protected URLTriggerContentType type;

    protected void initForContent(String content) throws XTriggerException {
        type.initForContent(content, log);
    }

    protected boolean isTriggeringBuildForContent(String content) throws XTriggerException {
        return type.isTriggeringBuildForContent(content, log);
    }
}
