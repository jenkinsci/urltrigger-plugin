package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractXMLContentTypeTest extends AbstractContentTypeTest {

    protected String getAnyContent() throws IOException, URISyntaxException {
        return readXMLContent("xml/anyXml.xml");
    }

    protected String getEmptyTypeContent() throws IOException, URISyntaxException {
        return readXMLContent("xml/emptyXml.xml");
    }

    @Test(expected = Throwable.class)
    public void testInitForContentNoXML() throws URLTriggerException {
        initForContent(new String("NO XML"));
    }
}