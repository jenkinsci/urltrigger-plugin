package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hudson.Util;
import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * @author Gregory Boissinot
 */
abstract class AbstractContentTypeTest extends AbstractURLTriggerContentTypeTest {

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    protected String readContentAsString(String relativeFilePath) throws URISyntaxException, IOException {
        return Util.loadFile(new File(this.getClass().getResource(relativeFilePath).toURI()), StandardCharsets.UTF_8);
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
    void testInitForContentNull() {
        assertThrows(XTriggerException.class, () -> initForContent(null));
    }

    @Disabled("TODO test currently fails")
    @Test
    void testInitForContentEmptyType() throws Exception {
        initForContent(getEmptyTypeContent());
    }

    @Test
    void testInitForContentAnyXML() throws Exception {
        initForContent(getAnyContent());
    }

    //---
    //--- IS TRIGGERED TEST
    //---

    @Test
    void testIsTriggeringBuildForContentWithChange_NullPreviousContent() throws Exception {
        String oldContent = null;
        String newContent = getNewContent();
        assertThrows(XTriggerException.class, () -> initForContent(oldContent));
        assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    void testIsTriggeringBuildForContentWithChange_EmptyPreviousContent() throws Exception {
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
