package org.jenkinsci.plugins.urltrigger.service;

import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.jenkinsci.plugins.xtriggerapi.XTriggerLog;
import org.jenkinsci.plugins.urltrigger.URLTriggerEntry;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerServiceTest {

    @Mock
    private Response clientResponseMock;

    private XTriggerLog log = new XTriggerLog(hudson.model.TaskListener.NULL);

    private URLTriggerService urlTriggerService;

    private AutoCloseable closeable;

    @Before
    public void init() {
        closeable = MockitoAnnotations.openMocks(this);
        urlTriggerService = URLTriggerService.getInstance();
        when(clientResponseMock.getStatusInfo()).thenReturn(Response.Status.OK);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void startStageCheckLastModificationDate() throws Exception {
        Date d1 = Calendar.getInstance().getTime();
        when(clientResponseMock.getLastModified()).thenReturn(d1);
        URLTriggerEntry urlTriggerEntry = new URLTriggerEntry();
        urlTriggerService.initContent(new HTTPResponse(clientResponseMock), urlTriggerEntry, log);
        assertThat(urlTriggerEntry.getLastModificationDate(), is(d1.getTime()));
    }

    @Test
    public void startStageCheckInternalContentNotCall() throws XTriggerException {
        Date stubDate = Calendar.getInstance().getTime();
        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(false);
        urlTriggerService.initContent(new HTTPResponse(clientResponseMock), urlTriggerEntryMock, log);
        verify(urlTriggerEntryMock, never()).getContentTypes();
    }

    @Test
    public void startStageCheckInternalContentCallWithNoContent() {
        Date stubDate = Calendar.getInstance().getTime();
        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);

        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(true);
        URLTriggerContentType urlTriggerContentTypeMock = mock(URLTriggerContentType.class);
        when(urlTriggerEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{urlTriggerContentTypeMock});
        when(clientResponseMock.readEntity(String.class)).thenReturn(null);

        assertThrows(XTriggerException.class, () -> urlTriggerService.initContent(new HTTPResponse(clientResponseMock), urlTriggerEntryMock, log));
    }


    @Test
    public void startStageCheckInternalContentCallWithContent() throws XTriggerException {

        Date stubDate = Calendar.getInstance().getTime();
        String contentStub = "CONTENT";

        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);

        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(true);
        URLTriggerContentType urlTriggerContentTypeMock = mock(URLTriggerContentType.class);
        when(urlTriggerEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{urlTriggerContentTypeMock});
        when(clientResponseMock.readEntity(String.class)).thenReturn(contentStub);

        urlTriggerService.initContent(new HTTPResponse(clientResponseMock), urlTriggerEntryMock, log);

        verify(urlTriggerEntryMock, times(1)).getContentTypes();
        verify(urlTriggerContentTypeMock, times(1)).initForContent(contentStub, log);
    }


    @Test
    public void isSchedulingForURLEntry_checkStatus() throws XTriggerException {

        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);

        int status = 200;
        when(urlEntryMock.isCheckStatus()).thenReturn(true);
        when(urlEntryMock.getStatusCode()).thenReturn(status);
        when(clientResponseMock.getStatus()).thenReturn(status);

        boolean result = urlTriggerService.isSchedulingAndGetRefresh(new HTTPResponse(clientResponseMock), urlEntryMock, mock(XTriggerLog.class));
        assertTrue(result);

        verify(clientResponseMock, times(1)).getStatus();
        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, times(1)).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
    }


    private void setWhenIsSchedulingForURLEntryCheckLastModificationDate(URLTriggerEntry urlEntryMock, long entryLastModificationDate, Date responseDate) {
        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(true);
        when(urlEntryMock.getLastModificationDate()).thenReturn(entryLastModificationDate);
        when(clientResponseMock.getLastModified()).thenReturn(responseDate);
    }


    private void verifyIsSchedulingForURLEntryCheckLastModificationDate(URLTriggerEntry urlEntryMock, Date responseDate) {
        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, times(1)).getLastModified();
        verify(clientResponseMock, never()).readEntity(String.class);

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, times(1)).getLastModificationDate();
        verify(urlEntryMock, times(1)).setLastModificationDate(responseDate.getTime());
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, never()).getContentTypes();
    }

    @Test
    public void isSchedulingForURLEntry_checkSLastModificationDate_1() throws XTriggerException {

        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        Date responseDate = Calendar.getInstance().getTime();

        setWhenIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, 0L, responseDate);
        boolean result = urlTriggerService.isSchedulingAndGetRefresh(new HTTPResponse(clientResponseMock), urlEntryMock, mock(XTriggerLog.class));
        assertFalse(result);
        verifyIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, responseDate);
    }

    @Test
    public void isSchedulingForURLEntry_checkSLastModificationDate_2() throws XTriggerException {

        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        Date responseDate = Calendar.getInstance().getTime();

        setWhenIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, 1L, responseDate);
        boolean result = urlTriggerService.isSchedulingAndGetRefresh(new HTTPResponse(clientResponseMock), urlEntryMock, mock(XTriggerLog.class));
        assertTrue(result);
        verifyIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, responseDate);
    }

    @Test
    public void isSchedulingForURLEntry_checkContent_1() throws XTriggerException {
        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);

        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckETag()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(false);
        when(urlEntryMock.isInspectingContent()).thenReturn(true);
        when(urlEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{});

        boolean result = urlTriggerService.isSchedulingAndGetRefresh(new HTTPResponse(clientResponseMock), urlEntryMock, mock(XTriggerLog.class));
        assertFalse(result);

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckETag();
        verify(urlEntryMock, never()).getETag();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, never()).getLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, times(1)).getContentTypes();

        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, never()).getLastModified();
        verify(clientResponseMock, times(1)).readEntity(String.class);
    }

    @Test
    public void isSchedulingForURLEntry_checkContent_2() throws XTriggerException {
        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        URLTriggerContentType contentTypeMock = mock(URLTriggerContentType.class);

        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(false);
        when(urlEntryMock.isInspectingContent()).thenReturn(true);
        when(urlEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{contentTypeMock});
        when(clientResponseMock.readEntity(ArgumentMatchers.any(Class.class))).thenReturn(null);

        urlTriggerService.isSchedulingAndGetRefresh(new HTTPResponse(clientResponseMock), urlEntryMock, mock(XTriggerLog.class));
        assertFalse(false);

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, never()).getLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, times(1)).getContentTypes();

        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, never()).getLastModified();
        verify(clientResponseMock, times(1)).readEntity(String.class);

        verify(contentTypeMock, never()).isTriggering(anyString(), any(XTriggerLog.class));
    }

    private void checkContent(boolean isTriggeringBuildContent, boolean expectedResult) throws XTriggerException {
        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        URLTriggerContentType contentTypeMock = mock(URLTriggerContentType.class);

        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(false);
        when(urlEntryMock.isInspectingContent()).thenReturn(true);
        when(urlEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{contentTypeMock});
        when(clientResponseMock.readEntity(ArgumentMatchers.any(Class.class))).thenReturn("S");
        when(contentTypeMock.isTriggering(ArgumentMatchers.any(String.class), ArgumentMatchers.any(XTriggerLog.class))).thenReturn(isTriggeringBuildContent);

        boolean result = urlTriggerService.isSchedulingAndGetRefresh(new HTTPResponse(clientResponseMock), urlEntryMock, mock(XTriggerLog.class));
        assertEquals(expectedResult, result);

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, never()).getLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, times(2)).getContentTypes();

        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, never()).getLastModified();
        verify(clientResponseMock, times(1)).readEntity(String.class);

        verify(contentTypeMock, times(1)).isTriggering(anyString(), any(XTriggerLog.class));
    }

    @Test
    public void isSchedulingForURLEntry_checkContent_3() throws XTriggerException {
        checkContent(false, false);
    }

    @Test
    public void isSchedulingForURLEntry_checkContent_4() throws XTriggerException {
        checkContent(true, true);
    }
}
