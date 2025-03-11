package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
abstract class AbstractXMLContentTypeTest extends AbstractContentTypeTest {

    protected String getAnyContent() throws IOException, URISyntaxException {
        return readContentAsString("xml/anyXml.xml");
    }

    protected String getEmptyTypeContent() throws IOException, URISyntaxException {
        return readContentAsString("xml/emptyXml.xml");
    }

    @Test
    void testInitForContentEmpty() {
        assertThrows(XTriggerException.class, () -> initForContent(getEmptyContent()));
    }

    @Test
    void testInitForContentNoXML() {
        assertThrows(XTriggerException.class, () -> initForContent("NO XML"));
    }

}
