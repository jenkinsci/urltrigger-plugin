package org.jenkinsci.plugins.urltrigger.content;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class JSONArrayContentTypeWithExpressionsTest extends AbstractJSONContentTypeTest {

    public JSONArrayContentTypeWithExpressionsTest() {
        JSONContentEntry[] expressions = new JSONContentEntry[]{new JSONContentEntry("$..name")};
        type = new JSONContentType(Arrays.asList(expressions));
    }

    protected String getOldContentNotEmpty() throws IOException, URISyntaxException {
        return readContentAsString("json/expressions/oldGitHubContent.json");
    }

    protected String getNewContent() throws IOException, URISyntaxException {
        return readContentAsString("json/expressions/newGitHubContent.json");
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

