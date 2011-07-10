package org.jenkinsci.plugins.urltrigger.content;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class XMLFileContentEntry implements Serializable {

    private String expression;

    @DataBoundConstructor
    public XMLFileContentEntry(String expression) {
        this.expression = expression;
    }

    @SuppressWarnings("unused")
    public String getExpression() {
        return expression;
    }
}
