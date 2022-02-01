package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.Assert.assertThrows;

import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
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

    @Test
    public void testInitForContentEmpty() {
        assertThrows(XTriggerException.class, () -> initForContent(getEmptyContent()));
    }

    @Test
    public void testInitForContentNoJSON() {
        assertThrows(XTriggerException.class, () -> initForContent("NO JSON"));
    }
}
