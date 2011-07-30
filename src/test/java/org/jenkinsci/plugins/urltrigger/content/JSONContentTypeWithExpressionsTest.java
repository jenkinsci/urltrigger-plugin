package org.jenkinsci.plugins.urltrigger.content;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * @author Gregory Boissinot
 */
public class JSONContentTypeWithExpressionsTest extends AbstractJSONContentTypeTest {

    public JSONContentTypeWithExpressionsTest() {
        JSONContentEntry[] expressions = new JSONContentEntry[]{new JSONContentEntry("$.store.book[0].title")};
        type = new JSONContentType(Arrays.<JSONContentEntry>asList(expressions));
    }

    protected String getOldContentNotEmpty() throws IOException, URISyntaxException {
        return readContentAsString("json/expressions/oldJsonContent.json");
    }

    protected String getNewContent() throws IOException, URISyntaxException {
        return readContentAsString("json/expressions/newJsonContent.json");
    }

    @Test
    public void testIsTriggeringMatchingNewContentWithEmptyPreviousContent() throws Exception {
        Assert.assertTrue(isTriggeringBuildForContentWithChange_EmptyTypePreviousContent());
    }

    @Test
    public void testIsTriggeringMatchingNewContentWithMatchingPreviousContent() throws Exception {
        Assert.assertTrue(isTriggeringBuildForContentWithChange_AnyContentPreviousContent());
    }
}

