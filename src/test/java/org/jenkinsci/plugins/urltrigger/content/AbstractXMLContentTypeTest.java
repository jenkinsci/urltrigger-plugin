package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.lib.xtrigger.XTriggerException;
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

    @Test(expected = XTriggerException.class)
    public void testInitForContentEmpty() throws Exception {
        initForContent(getEmptyContent());
    }

    @Test(expected = Throwable.class)
    public void testInitForContentNoXML() throws XTriggerException {
        initForContent(new String("NO XML"));
    }

}
