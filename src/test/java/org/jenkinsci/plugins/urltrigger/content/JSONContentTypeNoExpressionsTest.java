package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
class JSONContentTypeNoExpressionsTest extends AbstractJSONContentTypeTest {

    public JSONContentTypeNoExpressionsTest() {
        type = new JSONContentType(null);
    }

    protected String getOldContentNotEmpty() throws IOException, URISyntaxException {
        return readContentAsString("json/noExpressions/oldJsonContent.json");
    }

    protected String getNewContent() throws IOException, URISyntaxException {
        return readContentAsString("json/noExpressions/newJsonContent.json");
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

