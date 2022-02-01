package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.Assert.assertThrows;

import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractXMLContentTypeTest extends AbstractContentTypeTest {

    protected String getAnyContent() throws IOException, URISyntaxException {
        return readContentAsString("xml/anyXml.xml");
    }

    protected String getEmptyTypeContent() throws IOException, URISyntaxException {
        return readContentAsString("xml/emptyXml.xml");
    }

    @Test
    public void testInitForContentEmpty() {
        assertThrows(XTriggerException.class, () -> initForContent(getEmptyContent()));
    }

    @Test
    public void testInitForContentNoXML() {
        assertThrows(XTriggerException.class, () -> initForContent("NO XML"));
    }

}
