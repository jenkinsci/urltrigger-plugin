package org.jenkinsci.plugins.urltrigger.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author Gregory Boissinot
 */
public class SimpleContentTypeTest extends AbstractURLTriggerContentTypeTest {

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        type = new SimpleContentType();
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testInitForContentNull() {
        assertThrows(XTriggerException.class, () -> initForContent(null));
    }

    @Test
    public void testInitForContentEmpty() {
        assertThrows(XTriggerException.class, () -> initForContent(""));
    }

    @Test
    public void testInitForContentAnyString() throws XTriggerException {
        initForContent("Any string");
    }

    @Test
    public void testIsTriggeringBuildForContentWithNoChange_NullPreviousContent() throws XTriggerException {
        String content = null;
        assertThrows(XTriggerException.class, () -> initForContent(content));
        assertFalse(isTriggeringBuildForContent(content));
    }

    @Test
    public void testIsTriggeringBuildForContentWithNoChange_EmptyPreviousContent() throws XTriggerException {
        String content = "";
        assertThrows(XTriggerException.class, () -> initForContent(content));
        assertFalse(isTriggeringBuildForContent(content));
    }

    @Test
    public void testIsTriggeringBuildForContentWithNoChange_AnyStringPreviousContent() throws XTriggerException {
        String content = "AnyString";
        initForContent(content);
        assertFalse(isTriggeringBuildForContent(content));
    }

    private String getNewContent(String oldContent) {
        return oldContent + "AddedContent";
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_NullPreviousContent() throws XTriggerException {
        String oldContent = null;
        String newContent = getNewContent(oldContent);
        assertThrows(XTriggerException.class, () -> initForContent(oldContent));
        assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_EmptyPreviousContent() throws XTriggerException {
        String oldContent = "";
        String newContent = getNewContent(oldContent);
        assertThrows(XTriggerException.class, () -> initForContent(oldContent));
        assertFalse(isTriggeringBuildForContent(newContent));
    }

    @Test
    public void testIsTriggeringBuildForContentWithChange_AnyStringPreviousContent() throws XTriggerException {
        String oldContent = "AnyString";
        String newContent = getNewContent(oldContent);
        initForContent(oldContent);
        assertTrue(isTriggeringBuildForContent(newContent));
    }

}
