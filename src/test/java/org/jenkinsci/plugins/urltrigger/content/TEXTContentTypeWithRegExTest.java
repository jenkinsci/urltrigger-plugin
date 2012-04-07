package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.junit.Assert;
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
        type = new TEXTContentType(Arrays.<TEXTContentEntry>asList(regExElements));
    }

    @Override
    protected String getAnyContent() throws IOException, URISyntaxException {
        return new String("ANY CONTENT TXT");
    }

    @Override
    protected String getEmptyTypeContent() throws IOException, URISyntaxException {
        return new String();
    }

    @Override
    protected String getOldContentNotEmpty() throws IOException, URISyntaxException {
        return readContentAsString("txt/matches/oldLog.txt");
    }

    @Override
    protected String getNewContent() throws IOException, URISyntaxException {
        return readContentAsString("txt/matches/newLog.txt");
    }

    @Test(expected = XTriggerException.class)
    public void testInitForContentEmpty() throws Exception {
        initForContent(getEmptyContent());
    }

    @Test(expected = XTriggerException.class)
    public void testIsTriggeringMatchingNewContentWithEmptyPreviousContent() throws Exception {
        Assert.assertTrue(isTriggeringBuildForContentWithChange_EmptyTypePreviousContent());
    }

    @Test
    public void testIsTriggeringMatchingNewContentWithMatchingPreviousContent() throws Exception {
        Assert.assertTrue(isTriggeringBuildForContentWithChange_AnyContentPreviousContent());
    }

}
