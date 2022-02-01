package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * @author Gregory Boissinot
 */
public class TEXTContentTypeWithRegExTest extends AbstractContentTypeTest {

    public TEXTContentTypeWithRegExTest() {
        TEXTContentEntry[] regExElements = new TEXTContentEntry[]{new TEXTContentEntry("^ERROR\\w+$")};
        type = new TEXTContentType(Arrays.asList(regExElements));
    }

    @Override
    protected String getAnyContent() {
        return "ANY CONTENT TXT";
    }

    @Override
    protected String getEmptyTypeContent() {
        return "";
    }

    @Override
    protected String getOldContentNotEmpty() throws IOException, URISyntaxException {
        return readContentAsString("txt/matches/oldLog.txt");
    }

    @Override
    protected String getNewContent() throws IOException, URISyntaxException {
        return readContentAsString("txt/matches/newLog.txt");
    }

    @Test
    public void testInitForContentEmpty() {
        assertThrows(XTriggerException.class, () -> initForContent(getEmptyContent()));
    }

    @Test
    public void testIsTriggeringMatchingNewContentWithEmptyPreviousContent() {
        assertThrows(XTriggerException.class, this::isTriggeringBuildForContentWithChange_EmptyTypePreviousContent);
    }

    @Test
    public void testIsTriggeringMatchingNewContentWithMatchingPreviousContent() throws Exception {
        assertTrue(isTriggeringBuildForContentWithChange_AnyContentPreviousContent());
    }

}
