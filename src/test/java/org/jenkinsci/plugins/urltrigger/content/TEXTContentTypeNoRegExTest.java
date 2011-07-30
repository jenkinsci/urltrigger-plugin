package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.plugins.urltrigger.URLTriggerException;
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

    @Test(expected = URLTriggerException.class)
    public void testInitForContentNull() throws URLTriggerException {
        initForContent(null);
    }

    @Test
    public void testInitForContentEmpty() throws URLTriggerException {
        initForContent(new String());
        Assert.assertTrue(true);
    }

    @Test
    public void testInitForContentAnyString() throws URLTriggerException {
        initForContent(new String("Any string"));
        Assert.assertTrue(true);
    }

    @Test(expected = URLTriggerException.class)
    public void testIsTriggeringBuildForContentWithNoChange_NullPreviousContent() throws URLTriggerException {
        String content = null;
        initForContent(content);
        Assert.assertTrue(isTriggeringBuildForContent(content));
    }

    @Test
    public void testIsTriggeringBuildForContentWithNoChange_EmptyPreviousContent() throws URLTriggerException {
        String content = new String();
        initForContent(content);
        Assert.assertFalse(isTriggeringBuildForContent(content));
    }

    @Test
    public void testIsTriggeringBuildForContentWithNoChange_AnyStringPreviousContent() throws URLTriggerException {
        String content = new String("AnyString");
        initForContent(content);
        Assert.assertFalse(isTriggeringBuildForContent(content));
    }

    private String getNewAnyContent(String oldContent) {
        return oldContent + "AddedAnyContent";
    }

    @Test(expected = URLTriggerException.class)
    public void testIsTriggeringBuildForAnyContent_NullPreviousContent() throws URLTriggerException {
        String oldContent = null;
        String newContent = getNewAnyContent(oldContent);
        initForContent(oldContent);
        Assert.assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    public void testIsTriggeringBuildForAnyContent_EmptyPreviousContent() throws URLTriggerException {
        String oldContent = new String();
        String newContent = getNewAnyContent(oldContent);
        initForContent(oldContent);
        Assert.assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    public void testIsTriggeringBuildForAnyNewContent_AnyPreviousContent() throws URLTriggerException {
        String oldContent = new String("AnyString");
        String newContent = getNewAnyContent(oldContent);
        initForContent(oldContent);
        Assert.assertFalse(isTriggeringBuildForContent(newContent));
    }

}