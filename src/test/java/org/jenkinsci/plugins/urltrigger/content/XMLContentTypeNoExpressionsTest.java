package org.jenkinsci.plugins.urltrigger.content;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
public class XMLContentTypeNoExpressionsTest extends AbstractXMLContentTypeTest {

    public XMLContentTypeNoExpressionsTest() {
        type = new XMLContentType(null);
    }

    protected String getOldContentNotEmpty() throws IOException, URISyntaxException {
        return readContentAsString("xml/noExpressions/oldXmlContent.xml");
    }

    protected String getNewContent() throws IOException, URISyntaxException {
        return readContentAsString("xml/noExpressions/newXmlContent.xml");
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_EmptyTypePreviousContent() throws Exception {
        Assert.assertFalse(isTriggeringBuildForContentWithChange_EmptyTypePreviousContent());
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_AnyContentPreviousContent() throws Exception {
        Assert.assertFalse(isTriggeringBuildForContentWithChange_AnyContentPreviousContent());
    }

}
