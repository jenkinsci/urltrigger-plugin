package org.jenkinsci.plugins.urltrigger;

import com.sun.jersey.api.client.ClientResponse;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerTest {

    @Mock
    private ClientResponse clientResponse;

    @Mock
    private AbstractProject project;

    @Mock
    private Hudson hudson;

    private String validCron;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        validCron = "* * * * *";
    }

    public void testStartEmptyEntries() throws Exception {
        List<URLTriggerEntry> entries = new ArrayList<URLTriggerEntry>();
        URLTrigger urlTrigger = new URLTrigger(validCron, entries);
        urlTrigger.start(project, false);
        Assert.assertTrue(true);
    }
}
