package org.jenkinsci.plugins.urltrigger.service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.jenkinsci.plugins.urltrigger.URLTriggerEntry;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
    public void checkLastModificationDate() throws Exception {
        Date d1 = Calendar.getInstance().getTime();
        when(clientResponseMock.getLastModified()).thenReturn(d1);
        URLTriggerEntry urlTriggerEntry = new URLTriggerEntry();
        urlTriggerService.processEntry(clientResponseMock, urlTriggerEntry);
        Assert.assertThat(urlTriggerEntry.getLastModificationDate(), is(d1.getTime()));
    }

    @Test
    public void checkInternalContentNotCall() throws URLTriggerException {
        Date stubDate = Calendar.getInstance().getTime();
        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(false);
        urlTriggerService.processEntry(clientResponseMock, urlTriggerEntryMock);
        verify(urlTriggerEntryMock, never()).getContentTypes();
    }

    @Test(expected = URLTriggerException.class)
    public void checkInternalContentCallWithNoContent() throws URLTriggerException {
        Date stubDate = Calendar.getInstance().getTime();
        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);

        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(true);
        URLTriggerContentType urlTriggerContentTypeMock = mock(URLTriggerContentType.class);
        when(urlTriggerEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{urlTriggerContentTypeMock});
        when(clientResponseMock.getEntity(String.class)).thenReturn(null);

        urlTriggerService.processEntry(clientResponseMock, urlTriggerEntryMock);
    }


    @Test
    public void checkInternalContentCallWithContent() throws URLTriggerException {

        Date stubDate = Calendar.getInstance().getTime();
        String contentStub = "CONTENT";

        URLTriggerEntry urlTriggerEntryMock = mock(URLTriggerEntry.class);

        when(clientResponseMock.getLastModified()).thenReturn(stubDate);
        when(urlTriggerEntryMock.isInspectingContent()).thenReturn(true);
        URLTriggerContentType urlTriggerContentTypeMock = mock(URLTriggerContentType.class);
        when(urlTriggerEntryMock.getContentTypes()).thenReturn(new URLTriggerContentType[]{urlTriggerContentTypeMock});
        when(clientResponseMock.getEntity(String.class)).thenReturn(contentStub);

        urlTriggerService.processEntry(clientResponseMock, urlTriggerEntryMock);

        verify(urlTriggerEntryMock, times(1)).getContentTypes();
        verify(urlTriggerContentTypeMock, times(1)).initForContent(contentStub);
    }

}
