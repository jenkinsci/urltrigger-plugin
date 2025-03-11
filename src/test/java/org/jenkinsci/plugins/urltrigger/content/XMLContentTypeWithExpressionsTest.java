package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * @author Gregory Boissinot
 */
class XMLContentTypeWithExpressionsTest extends AbstractXMLContentTypeTest {

    public XMLContentTypeWithExpressionsTest() {
        XMLContentEntry[] expressions = new XMLContentEntry[]{new XMLContentEntry("/employees/employee[1]/name")};
        type = new XMLContentType(Arrays.asList(expressions));
    }

    protected String getOldContentNotEmpty() throws IOException, URISyntaxException {
        return readContentAsString("xml/expressions/oldXmlContent.xml");
    }

    protected String getNewContent() throws IOException, URISyntaxException {
        return readContentAsString("xml/expressions/newXmlContent.xml");
    }

    @Test
    void testIsTriggeringMatchingNewContentWithEmptyPreviousContent() throws Exception {
        assertTrue(isTriggeringBuildForContentWithChange_EmptyTypePreviousContent());
    }

    @Test
    void testIsTriggeringMatchingNewContentWithMatchingPreviousContent() throws Exception {
        assertTrue(isTriggeringBuildForContentWithChange_AnyContentPreviousContent());
    }

}
