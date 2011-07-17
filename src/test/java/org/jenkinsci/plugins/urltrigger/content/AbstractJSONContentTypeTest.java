package org.jenkinsci.plugins.urltrigger.content;

import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractJSONContentTypeTest extends AbstractContentTypeTest {

    protected String getAnyContent() throws IOException, URISyntaxException {
        return readXMLContent("json/anyJson.json");
    }

    protected String getEmptyTypeContent() throws IOException, URISyntaxException {
        return readXMLContent("json/emptyJson.json");
    }

    @Test(expected = Throwable.class)
    public void testInitForContentNoJSON() throws URLTriggerException {
        initForContent(new String("NO JSON"));
    }
}
