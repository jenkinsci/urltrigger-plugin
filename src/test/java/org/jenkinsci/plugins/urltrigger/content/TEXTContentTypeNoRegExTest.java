package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author Gregory Boissinot
 */
class TEXTContentTypeNoRegExTest extends AbstractURLTriggerContentTypeTest {

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        type = new TEXTContentType(null);
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

    private String getNewAnyContent(String oldContent) {
        return oldContent + "AddedAnyContent";
    }

    @Test
    void testIsTriggeringBuildForAnyContent_NullPreviousContent() throws XTriggerException {
        String oldContent = null;
        String newContent = getNewAnyContent(oldContent);
        assertThrows(XTriggerException.class, () -> initForContent(oldContent));
        assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    void testIsTriggeringBuildForAnyContent_EmptyPreviousContent() throws XTriggerException {
        String oldContent = "";
        String newContent = getNewAnyContent(oldContent);
        assertThrows(XTriggerException.class, () -> initForContent(oldContent));
        assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    void testIsTriggeringBuildForAnyNewContent_AnyPreviousContent() throws XTriggerException {
        String oldContent = "AnyString";
        String newContent = getNewAnyContent(oldContent);
        initForContent(oldContent);
        assertFalse(isTriggeringBuildForContent(newContent));
    }

}