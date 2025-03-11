package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.jupiter.api.Assertions.*;

import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author Gregory Boissinot
 */
class SimpleContentTypeTest extends AbstractURLTriggerContentTypeTest {

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        type = new SimpleContentType();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testInitForContentNull() {
        assertThrows(XTriggerException.class, () -> initForContent(null));
    }

    @Test
    void testInitForContentEmpty() {
        assertThrows(XTriggerException.class, () -> initForContent(""));
    }

    @Test
    void testInitForContentAnyString() throws XTriggerException {
        initForContent("Any string");
    }

    @Test
    void testIsTriggeringBuildForContentWithNoChange_NullPreviousContent() throws XTriggerException {
        String content = null;
        assertThrows(XTriggerException.class, () -> initForContent(content));
        assertFalse(isTriggeringBuildForContent(content));
    }

    @Test
    void testIsTriggeringBuildForContentWithNoChange_EmptyPreviousContent() throws XTriggerException {
        String content = "";
        assertThrows(XTriggerException.class, () -> initForContent(content));
        assertFalse(isTriggeringBuildForContent(content));
    }

    @Test
    void testIsTriggeringBuildForContentWithNoChange_AnyStringPreviousContent() throws XTriggerException {
        String content = "AnyString";
        initForContent(content);
        assertFalse(isTriggeringBuildForContent(content));
    }

    private String getNewContent(String oldContent) {
        return oldContent + "AddedContent";
    }

    @Test
    void testIsTriggeringBuildForContentWithChange_NullPreviousContent() throws XTriggerException {
        String oldContent = null;
        String newContent = getNewContent(oldContent);
        assertThrows(XTriggerException.class, () -> initForContent(oldContent));
        assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    void testIsTriggeringBuildForContentWithChange_EmptyPreviousContent() throws XTriggerException {
        String oldContent = "";
        String newContent = getNewContent(oldContent);
        assertThrows(XTriggerException.class, () -> initForContent(oldContent));
        assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    void testIsTriggeringBuildForContentWithChange_AnyStringPreviousContent() throws XTriggerException {
        String oldContent = "AnyString";
        String newContent = getNewContent(oldContent);
        initForContent(oldContent);
        assertTrue(isTriggeringBuildForContent(newContent));
    }

}
