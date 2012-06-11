package org.jenkinsci.plugins.urltrigger.service;

import com.sun.jersey.api.client.ClientResponse;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.urltrigger.URLTriggerEntry;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerServiceTest {

    @Mock
    private ClientResponse clientResponseMock;

    private XTriggerLog log = new XTriggerLog((StreamTaskListener) TaskListener.NULL);

    private URLTriggerService urlTriggerService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        urlTriggerService = URLTriggerService.getInstance();
        when(clientResponseMock.getClientResponseStatus()).thenReturn(ClientResponse.Status.OK);
    }

    @Test
    public void startStageCheckLastModificationDate() throws Exception {
        Date d1 = Calendar.getInstance().getTime();
        when(clientResponseMock.getLastModified()).thenReturn(d1);
        URLTriggerEntry urlTriggerEntry = new URLTriggerEntry();
        urlTriggerService.initContent(clientResponseMock, urlTriggerEntry, log);
        Assert.assertThat(urlTriggerEntry.getLastModificationDate(), is(d1.getTime()));
    }

    @Test
    public void startStageCheckInternalContentNotCall() throws XTriggerException {
        Date stubDate = Calendar.getInstance().getTime();
        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(false);
        urlTriggerService.initContent(clientResponseMock, urlTriggerEntryMock, log);
        verify(urlTriggerEntryMock, never()).getContentTypes();
    }

    @Test(expected = XTriggerException.class)
    public void startStageCheckInternalContentCallWithNoContent() throws XTriggerException {
        Date stubDate = Calendar.getInstance().getTime();
        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);

        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(true);
        URLTriggerContentType urlTriggerContentTypeMock = mock(URLTriggerContentType.class);
        when(urlTriggerEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{urlTriggerContentTypeMock});
        when(clientResponseMock.getEntity(String.class)).thenReturn(null);

        urlTriggerService.initContent(clientResponseMock, urlTriggerEntryMock, log);
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
        when(clientResponseMock.getEntity(String.class)).thenReturn(contentStub);

        urlTriggerService.initContent(clientResponseMock, urlTriggerEntryMock, log);

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

        boolean result = urlTriggerService.isSchedulingAndGetRefresh(clientResponseMock, urlEntryMock, mock(XTriggerLog.class));
        Assert.assertTrue(result);

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
        verify(clientResponseMock, never()).getEntity(String.class);

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
        boolean result = urlTriggerService.isSchedulingAndGetRefresh(clientResponseMock, urlEntryMock, mock(XTriggerLog.class));
        Assert.assertFalse(result);
        verifyIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, responseDate);
    }

    @Test
    public void isSchedulingForURLEntry_checkSLastModificationDate_2() throws XTriggerException {

        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        Date responseDate = Calendar.getInstance().getTime();

        setWhenIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, 1L, responseDate);
        boolean result = urlTriggerService.isSchedulingAndGetRefresh(clientResponseMock, urlEntryMock, mock(XTriggerLog.class));
        Assert.assertTrue(result);
        verifyIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, responseDate);
    }

    @Test
    public void isSchedulingForURLEntry_checkContent_1() throws XTriggerException {
        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);

        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(false);
        when(urlEntryMock.isInspectingContent()).thenReturn(true);
        when(urlEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{});

        boolean result = urlTriggerService.isSchedulingAndGetRefresh(clientResponseMock, urlEntryMock, mock(XTriggerLog.class));
        Assert.assertFalse(result);

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, never()).getLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, times(1)).getContentTypes();

        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, never()).getLastModified();
        verify(clientResponseMock, times(1)).getEntity(String.class);
    }

    @Test
    public void isSchedulingForURLEntry_checkContent_2() throws XTriggerException {
        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        URLTriggerContentType contentTypeMock = mock(URLTriggerContentType.class);

        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(false);
        when(urlEntryMock.isInspectingContent()).thenReturn(true);
        when(urlEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{contentTypeMock});
        when(clientResponseMock.getEntity(Matchers.any(Class.class))).thenReturn(null);

        urlTriggerService.isSchedulingAndGetRefresh(clientResponseMock, urlEntryMock, mock(XTriggerLog.class));
        Assert.assertFalse(false);

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, never()).getLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, times(1)).getContentTypes();

        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, never()).getLastModified();
        verify(clientResponseMock, times(1)).getEntity(String.class);

        verify(contentTypeMock, never()).isTriggering(anyString(), any(XTriggerLog.class));
    }

    private void checkContent(boolean isTriggeringBuildContent, boolean expectedResult) throws XTriggerException {
        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        URLTriggerContentType contentTypeMock = mock(URLTriggerContentType.class);

        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(false);
        when(urlEntryMock.isInspectingContent()).thenReturn(true);
        when(urlEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{contentTypeMock});
        when(clientResponseMock.getEntity(Matchers.any(Class.class))).thenReturn("S");
        when(contentTypeMock.isTriggering(Matchers.any(String.class), Matchers.any(XTriggerLog.class))).thenReturn(isTriggeringBuildContent);

        boolean result = urlTriggerService.isSchedulingAndGetRefresh(clientResponseMock, urlEntryMock, mock(XTriggerLog.class));
        Assert.assertEquals(expectedResult, result);

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, never()).getLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, times(2)).getContentTypes();

        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, never()).getLastModified();
        verify(clientResponseMock, times(1)).getEntity(String.class);

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
