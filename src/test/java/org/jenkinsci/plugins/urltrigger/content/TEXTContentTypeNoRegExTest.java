package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author Gregory Boissinot
 */
public class TEXTContentTypeNoRegExTest extends AbstractURLTriggerContentTypeTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        type = new TEXTContentType(null);
    }

    @Test(expected = XTriggerException.class)
    public void testInitForContentNull() throws XTriggerException {
        initForContent(null);
    }

    @Test(expected = XTriggerException.class)
    public void testInitForContentEmpty() throws XTriggerException {
        initForContent(new String());
        Assert.assertTrue(true);
    }

    @Test
    public void testInitForContentAnyString() throws XTriggerException {
        initForContent(new String("Any string"));
        Assert.assertTrue(true);
    }

    @Test(expected = XTriggerException.class)
    public void testIsTriggeringBuildForContentWithNoChange_NullPreviousContent() throws XTriggerException {
        String content = null;
        initForContent(content);
        Assert.assertTrue(isTriggeringBuildForContent(content));
    }

    @Test(expected = XTriggerException.class)
    public void testIsTriggeringBuildForContentWithNoChange_EmptyPreviousContent() throws XTriggerException {
        String content = new String();
        initForContent(content);
        Assert.assertFalse(isTriggeringBuildForContent(content));
    }

    @Test
    public void testIsTriggeringBuildForContentWithNoChange_AnyStringPreviousContent() throws XTriggerException {
        String content = new String("AnyString");
        initForContent(content);
        Assert.assertFalse(isTriggeringBuildForContent(content));
    }

    private String getNewAnyContent(String oldContent) {
        return oldContent + "AddedAnyContent";
    }

    @Test(expected = XTriggerException.class)
    public void testIsTriggeringBuildForAnyContent_NullPreviousContent() throws XTriggerException {
        String oldContent = null;
        String newContent = getNewAnyContent(oldContent);
        initForContent(oldContent);
        Assert.assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test(expected = XTriggerException.class)
    public void testIsTriggeringBuildForAnyContent_EmptyPreviousContent() throws XTriggerException {
        String oldContent = new String();
        String newContent = getNewAnyContent(oldContent);
        initForContent(oldContent);
        Assert.assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    public void testIsTriggeringBuildForAnyNewContent_AnyPreviousContent() throws XTriggerException {
        String oldContent = new String("AnyString");
        String newContent = getNewAnyContent(oldContent);
        initForContent(oldContent);
        Assert.assertFalse(isTriggeringBuildForContent(newContent));
    }

}