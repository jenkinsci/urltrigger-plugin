package org.jenkinsci.plugins.urltrigger.content;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class JSONContentEntry {

    private String jsonPath;

    @DataBoundConstructor
    public JSONContentEntry(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    @SuppressWarnings("unused")
    public String getJsonPath() {
        return jsonPath;
    }
}
