package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
abstract class AbstractJSONContentTypeTest extends AbstractContentTypeTest {

    protected String getAnyContent() throws IOException, URISyntaxException {
        return readContentAsString("json/anyJson.json");
    }

    protected String getEmptyTypeContent() throws IOException, URISyntaxException {
        return readContentAsString("json/emptyJson.json");
    }

    @Test
    void testInitForContentEmpty() {
        assertThrows(XTriggerException.class, () -> initForContent(getEmptyContent()));
    }

    @Test
    void testInitForContentNoJSON() {
        assertThrows(XTriggerException.class, () -> initForContent("NO JSON"));
    }
}
