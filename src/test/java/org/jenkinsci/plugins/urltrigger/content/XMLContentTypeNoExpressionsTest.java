package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
class XMLContentTypeNoExpressionsTest extends AbstractXMLContentTypeTest {

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
    void testIsTriggeringBuildForContentWithChange_EmptyTypePreviousContent() throws Exception {
        assertFalse(isTriggeringBuildForContentWithChange_EmptyTypePreviousContent());
    }

    @Test
    void testIsTriggeringBuildForContentWithChange_AnyContentPreviousContent() throws Exception {
        assertFalse(isTriggeringBuildForContentWithChange_AnyContentPreviousContent());
    }

}
