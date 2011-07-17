package org.jenkinsci.plugins.urltrigger.content;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * @author Gregory Boissinot
 */
public class XMLContentTypeWithExpressionsTest extends AbstractXMLContentTypeTest {

    public XMLContentTypeWithExpressionsTest() {
        XMLContentEntry[] expressions = new XMLContentEntry[]{new XMLContentEntry("/employees/employee[1]/name")};
        type = new XMLContentType(Arrays.<XMLContentEntry>asList(expressions));
    }

    protected String getOldContentNotEmpty() throws IOException, URISyntaxException {
        return readXMLContent("xml/expressions/oldXmlContent.xml");
    }

    protected String getNewContent() throws IOException, URISyntaxException {
        return readXMLContent("xml/expressions/newXmlContent.xml");
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_EmptyTypePreviousContent() throws Exception {
        Assert.assertTrue(isTriggeringBuildForContentWithChange_EmptyTypePreviousContent());
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_AnyContentPreviousContent() throws Exception {
        Assert.assertTrue(isTriggeringBuildForContentWithChange_AnyContentPreviousContent());
    }

}
