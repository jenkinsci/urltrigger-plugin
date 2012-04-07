package org.jenkinsci.plugins.urltrigger.content;

import hudson.Util;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractContentTypeTest extends AbstractURLTriggerContentTypeTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    protected String readContentAsString(String relativeFilePath) throws URISyntaxException, IOException {
        return Util.loadFile(new File(this.getClass().getResource(relativeFilePath).toURI()));
    }

    protected String getEmptyContent() {
        return new String();
    }

    protected abstract String getAnyContent() throws IOException, URISyntaxException;

    protected abstract String getEmptyTypeContent() throws IOException, URISyntaxException;

    protected abstract String getOldContentNotEmpty() throws IOException, URISyntaxException;

    protected abstract String getNewContent() throws IOException, URISyntaxException;

    //---
    //--- INIT CONTENT TESTS
    //---

    @Test(expected = Throwable.class)
    public void testInitForContentNull() throws XTriggerException {
        initForContent(null);
    }

    public void testInitForContentEmptyType() throws Exception {
        initForContent(getEmptyTypeContent());
        Assert.assertTrue(true);
    }

    @Test
    public void testInitForContentAnyXML() throws Exception {
        initForContent(getAnyContent());
        Assert.assertTrue(true);
    }

    //---
    //--- IS TRIGGERED TEST
    //---

    @Test(expected = Throwable.class)
    public void testIsTriggeringBuildForContentWithChange_NullPreviousContent() throws Exception {
        String oldContent = null;
        String newContent = getNewContent();
        initForContent(oldContent);
        Assert.assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test(expected = XTriggerException.class)
    public void testIsTriggeringBuildForContentWithChange_EmptyPreviousContent() throws Exception {
        String oldContent = getEmptyContent();
        String newContent = getNewContent();
        initForContent(oldContent);
        Assert.assertFalse(isTriggeringBuildForContent(newContent));
    }

    protected boolean isTriggeringBuildForContentWithChange_EmptyTypePreviousContent() throws Exception {
        String oldContent = getEmptyTypeContent();
        String newContent = getNewContent();
        initForContent(oldContent);
        return isTriggeringBuildForContent(newContent);
    }

    protected boolean isTriggeringBuildForContentWithChange_AnyContentPreviousContent() throws Exception {
        String oldContent = getOldContentNotEmpty();
        String newContent = getNewContent();
        initForContent(oldContent);
        return isTriggeringBuildForContent(newContent);
    }

}
