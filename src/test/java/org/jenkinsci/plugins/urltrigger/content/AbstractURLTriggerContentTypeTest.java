package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.mockito.Mock;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractURLTriggerContentTypeTest {

    @Mock
    protected XTriggerLog log;

    protected URLTriggerContentType type;

    protected void initForContent(String content) throws XTriggerException {
        type.initForContent(content);
    }

    protected boolean isTriggeringBuildForContent(String content) throws XTriggerException {
        return type.isTriggeringBuildForContent(content, log);
    }
}
