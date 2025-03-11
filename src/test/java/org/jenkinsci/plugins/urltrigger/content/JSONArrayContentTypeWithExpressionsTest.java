package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

class JSONArrayContentTypeWithExpressionsTest extends AbstractJSONContentTypeTest {

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
    void testIsTriggeringMatchingNewContentWithEmptyPreviousContent() throws Exception {
        assertTrue(isTriggeringBuildForContentWithChange_EmptyTypePreviousContent());
    }

    @Test
    void testIsTriggeringMatchingNewContentWithMatchingPreviousContent() throws Exception {
        assertTrue(isTriggeringBuildForContentWithChange_AnyContentPreviousContent());
    }
}

