package org.jenkinsci.plugins.urltrigger.service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.jenkinsci.plugins.urltrigger.URLTriggerEntry;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.URLTriggerLog;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerServiceTest {

    @Mock
    private Client clientMock;

    @Mock
    private ClientResponse clientResponseMock;

    private URLTriggerService urlTriggerService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        urlTriggerService = URLTriggerService.getInstance();
    }

    @Test
    public void startStageCheckLastModificationDate() throws Exception {
        Date d1 = Calendar.getInstance().getTime();
        when(clientResponseMock.getLastModified()).thenReturn(d1);
        URLTriggerEntry urlTriggerEntry = new URLTriggerEntry();
        urlTriggerService.processURLEntryFromStartStage(clientResponseMock, urlTriggerEntry);
        Assert.assertThat(urlTriggerEntry.getLastModificationDate(), is(d1.getTime()));
    }

    @Test
    public void startStageCheckInternalContentNotCall() throws URLTriggerException {
        Date stubDate = Calendar.getInstance().getTime();
        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(false);
        urlTriggerService.processURLEntryFromStartStage(clientResponseMock, urlTriggerEntryMock);
        verify(urlTriggerEntryMock, never()).getContentTypes();
    }

    @Test(expected = URLTriggerException.class)
    public void startStageCheckInternalContentCallWithNoContent() throws URLTriggerException {
        Date stubDate = Calendar.getInstance().getTime();
        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);

        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(true);
        URLTriggerContentType urlTriggerContentTypeMock = mock(URLTriggerContentType.class);
        when(urlTriggerEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{urlTriggerContentTypeMock});
        when(clientResponseMock.getEntity(String.class)).thenReturn(null);

        urlTriggerService.processURLEntryFromStartStage(clientResponseMock, urlTriggerEntryMock);
    }


    @Test
    public void startStageCheckInternalContentCallWithContent() throws URLTriggerException {

        Date stubDate = Calendar.getInstance().getTime();
        String contentStub = "CONTENT";

        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);

        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(true);
        URLTriggerContentType urlTriggerContentTypeMock = mock(URLTriggerContentType.class);
        when(urlTriggerEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{urlTriggerContentTypeMock});
        when(clientResponseMock.getEntity(String.class)).thenReturn(contentStub);

        urlTriggerService.processURLEntryFromStartStage(clientResponseMock, urlTriggerEntryMock);

        verify(urlTriggerEntryMock, times(1)).getContentTypes();
        verify(urlTriggerContentTypeMock, times(1)).initForContent(contentStub);
    }


    @Test
    public void isSchedulingForURLEntry_checkStatus() throws URLTriggerException {

        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);

        int status = 200;
        when(urlEntryMock.isCheckStatus()).thenReturn(true);
        when(urlEntryMock.getStatusCode()).thenReturn(status);
        when(clientResponseMock.getStatus()).thenReturn(status);

        boolean result = urlTriggerService.isSchedulingForURLEntry(clientResponseMock, urlEntryMock, mock(URLTriggerLog.class));
        Assert.assertTrue(result);

        verify(clientResponseMock, times(1)).getStatus();
        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, times(1)).getStatusCode();
        verify(urlEntryMock, never()).isInspectingContent();
        verify(urlEntryMock, never()).isCheckLastModificationDate();
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
        verify(urlEntryMock, never()).isInspectingContent();
        verify(urlEntryMock, never()).getContentTypes();
    }

    @Test
    public void isSchedulingForURLEntry_checkSLastModificationDate_1() throws URLTriggerException {

        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        Date responseDate = Calendar.getInstance().getTime();

        setWhenIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, 0L, responseDate);
        boolean result = urlTriggerService.isSchedulingForURLEntry(clientResponseMock, urlEntryMock, mock(URLTriggerLog.class));
        Assert.assertFalse(result);
        verifyIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, responseDate);
    }

    @Test
    public void isSchedulingForURLEntry_checkSLastModificationDate_2() throws URLTriggerException {

        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        Date responseDate = Calendar.getInstance().getTime();

        setWhenIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, 1L, responseDate);
        boolean result = urlTriggerService.isSchedulingForURLEntry(clientResponseMock, urlEntryMock, mock(URLTriggerLog.class));
        Assert.assertTrue(result);
        verifyIsSchedulingForURLEntryCheckLastModificationDate(urlEntryMock, responseDate);
    }

    @Test
    public void isSchedulingForURLEntry_checkContent_1() throws URLTriggerException {
        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);

        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(false);
        when(urlEntryMock.isInspectingContent()).thenReturn(true);
        when(urlEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{});

        boolean result = urlTriggerService.isSchedulingForURLEntry(clientResponseMock, urlEntryMock, mock(URLTriggerLog.class));
        Assert.assertFalse(result);

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, never()).getLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, times(1)).getContentTypes();

        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, never()).getLastModified();
        verify(clientResponseMock, never()).getEntity(String.class);
    }

    class TestContentType_Triggered extends URLTriggerContentType {

        @Override
        public void initForContent(String content) throws URLTriggerException {
        }

        @Override
        public boolean isTriggeringBuildForContent(String content, URLTriggerLog log) throws URLTriggerException {
            return true;
        }
    }

    class TestContentType_NotTriggered extends URLTriggerContentType {

        @Override
        public void initForContent(String content) throws URLTriggerException {
        }

        @Override
        public boolean isTriggeringBuildForContent(String content, URLTriggerLog log) throws URLTriggerException {
            return false;
        }
    }


    @Test
    public void isSchedulingForURLEntry_checkContent_2() throws URLTriggerException {
        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        URLTriggerContentType contentTypeMock = mock(URLTriggerContentType.class);

        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(false);
        when(urlEntryMock.isInspectingContent()).thenReturn(true);
        when(urlEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{contentTypeMock});
        when(clientResponseMock.getEntity(Matchers.any(Class.class))).thenReturn(null);

        try {
            urlTriggerService.isSchedulingForURLEntry(clientResponseMock, urlEntryMock, mock(URLTriggerLog.class));
            Assert.assertTrue(false);
        } catch (URLTriggerException urle) {
            Assert.assertTrue(true);
        }

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, never()).getLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, times(1)).getContentTypes();

        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, never()).getLastModified();
        verify(clientResponseMock, times(1)).getEntity(String.class);

        verify(contentTypeMock, never()).isTriggeringBuildForContent(anyString(), any(URLTriggerLog.class));
    }

    private void checkContent(boolean isTriggeringBuildContent, boolean expectedResult) throws URLTriggerException {
        URLTriggerEntry urlEntryMock = mock(URLTriggerEntry.class);
        URLTriggerContentType contentTypeMock = mock(URLTriggerContentType.class);

        when(urlEntryMock.isCheckStatus()).thenReturn(false);
        when(urlEntryMock.isCheckLastModificationDate()).thenReturn(false);
        when(urlEntryMock.isInspectingContent()).thenReturn(true);
        when(urlEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{contentTypeMock});
        when(clientResponseMock.getEntity(Matchers.any(Class.class))).thenReturn("S");
        when(contentTypeMock.isTriggeringBuildForContent(Matchers.any(String.class), Matchers.any(URLTriggerLog.class))).thenReturn(isTriggeringBuildContent);

        boolean result = urlTriggerService.isSchedulingForURLEntry(clientResponseMock, urlEntryMock, mock(URLTriggerLog.class));
        Assert.assertEquals(expectedResult, result);

        verify(urlEntryMock, times(1)).isCheckStatus();
        verify(urlEntryMock, never()).getStatusCode();
        verify(urlEntryMock, times(1)).isCheckLastModificationDate();
        verify(urlEntryMock, never()).getLastModificationDate();
        verify(urlEntryMock, times(1)).isInspectingContent();
        verify(urlEntryMock, times(1)).getContentTypes();

        verify(clientResponseMock, never()).getStatus();
        verify(clientResponseMock, never()).getLastModified();
        verify(clientResponseMock, times(1)).getEntity(String.class);

        verify(contentTypeMock, times(1)).isTriggeringBuildForContent(anyString(), any(URLTriggerLog.class));
    }

    @Test
    public void isSchedulingForURLEntry_checkContent_3() throws URLTriggerException {
        checkContent(false, false);
    }

    @Test
    public void isSchedulingForURLEntry_checkContent_4() throws URLTriggerException {
        checkContent(true, true);
    }
}
