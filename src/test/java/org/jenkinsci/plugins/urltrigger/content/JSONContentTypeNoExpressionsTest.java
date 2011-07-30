package org.jenkinsci.plugins.urltrigger.content;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
public class JSONContentTypeNoExpressionsTest extends AbstractJSONContentTypeTest {

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
    public void testIsTriggeringBuildForContentWithChange_EmptyTypePreviousContent() throws Exception {
        Assert.assertFalse(isTriggeringBuildForContentWithChange_EmptyTypePreviousContent());
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_AnyContentPreviousContent() throws Exception {
        Assert.assertFalse(isTriggeringBuildForContentWithChange_AnyContentPreviousContent());
    }

}

