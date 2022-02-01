package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import hudson.Util;
import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractContentTypeTest extends AbstractURLTriggerContentTypeTest {

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    protected String readContentAsString(String relativeFilePath) throws URISyntaxException, IOException {
        return Util.loadFile(new File(this.getClass().getResource(relativeFilePath).toURI()));
    }

    protected String getEmptyContent() {
        return "";
    }

    protected abstract String getAnyContent() throws IOException, URISyntaxException;

    protected abstract String getEmptyTypeContent() throws IOException, URISyntaxException;

    protected abstract String getOldContentNotEmpty() throws IOException, URISyntaxException;

    protected abstract String getNewContent() throws IOException, URISyntaxException;

    //---
    //--- INIT CONTENT TESTS
    //---

    @Test
    public void testInitForContentNull() {
        assertThrows(XTriggerException.class, () -> initForContent(null));
    }

    @Ignore("TODO test currently fails")
    @Test
    public void testInitForContentEmptyType() throws Exception {
        initForContent(getEmptyTypeContent());
    }

    @Test
    public void testInitForContentAnyXML() throws Exception {
        initForContent(getAnyContent());
    }

    //---
    //--- IS TRIGGERED TEST
    //---

    @Test
    public void testIsTriggeringBuildForContentWithChange_NullPreviousContent() throws Exception {
        String oldContent = null;
        String newContent = getNewContent();
        assertThrows(XTriggerException.class, () -> initForContent(oldContent));
        assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_EmptyPreviousContent() throws Exception {
        String oldContent = getEmptyContent();
        String newContent = getNewContent();
        assertThrows(XTriggerException.class, () -> initForContent(oldContent));
        assertFalse(isTriggeringBuildForContent(newContent));
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
