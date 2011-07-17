package org.jenkinsci.plugins.urltrigger.content;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class XMLContentEntry implements Serializable {

    private String xPath;

    @DataBoundConstructor
    public XMLContentEntry(String xPath) {
        this.xPath = xPath;
    }

    @SuppressWarnings("unused")
    public String getXPath() {
        return xPath;
    }
}
