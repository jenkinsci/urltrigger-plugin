package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author Gregory Boissinot
 */
public class SimpleContentTypeTest extends AbstractURLTriggerContentTypeTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        type = new SimpleContentType();
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

    @Test(expected = Throwable.class)
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

    private String getNewContent(String oldContent) {
        return oldContent + "AddedContent";
    }

    @Test(expected = XTriggerException.class)
    public void testIsTriggeringBuildForContentWithChange_NullPreviousContent() throws XTriggerException {
        String oldContent = null;
        String newContent = getNewContent(oldContent);
        initForContent(oldContent);
        Assert.assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test(expected = XTriggerException.class)
    public void testIsTriggeringBuildForContentWithChange_EmptyPreviousContent() throws XTriggerException {
        String oldContent = new String();
        String newContent = getNewContent(oldContent);
        initForContent(oldContent);
        Assert.assertTrue(isTriggeringBuildForContent(newContent));
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_AnyStringPreviousContent() throws XTriggerException {
        String oldContent = new String("AnyString");
        String newContent = getNewContent(oldContent);
        initForContent(oldContent);
        Assert.assertTrue(isTriggeringBuildForContent(newContent));
    }

}
