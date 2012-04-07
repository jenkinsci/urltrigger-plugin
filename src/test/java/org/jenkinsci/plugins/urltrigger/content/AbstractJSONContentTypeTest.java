package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractJSONContentTypeTest extends AbstractContentTypeTest {

    protected String getAnyContent() throws IOException, URISyntaxException {
        return readContentAsString("json/anyJson.json");
    }

    protected String getEmptyTypeContent() throws IOException, URISyntaxException {
        return readContentAsString("json/emptyJson.json");
    }

    @Test(expected = XTriggerException.class)
    public void testInitForContentEmpty() throws Exception {
        initForContent(getEmptyContent());
    }

    @Test(expected = Throwable.class)
    public void testInitForContentNoJSON() throws XTriggerException {
        initForContent(new String("NO JSON"));
    }
}
