package org.jenkinsci.plugins.urltrigger.content;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class XMLContentEntry implements Serializable {

    private String expression;

    @DataBoundConstructor
    public XMLContentEntry(String expression) {
        this.expression = expression;
    }

    @SuppressWarnings("unused")
    public String getExpression() {
        return expression;
    }
}
