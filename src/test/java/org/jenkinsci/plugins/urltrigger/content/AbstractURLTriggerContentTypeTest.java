package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.URLTriggerLog;
import org.mockito.Mock;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractURLTriggerContentTypeTest {

    @Mock
    protected URLTriggerLog log;

    protected URLTriggerContentType type;

    protected void initForContent(String content) throws URLTriggerException {
        type.initForContent(content);
    }

    protected boolean isTriggeringBuildForContent(String content) throws URLTriggerException {
        return type.isTriggeringBuildForContent(content, log);
    }
}
