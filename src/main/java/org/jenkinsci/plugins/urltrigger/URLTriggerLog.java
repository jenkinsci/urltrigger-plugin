package org.jenkinsci.plugins.urltrigger;

import hudson.model.TaskListener;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerLog {

    private TaskListener listener;

    public URLTriggerLog(TaskListener listener) {
        this.listener = listener;
    }

    public void info(String message) {
        listener.getLogger().println(message);
    }

    public void error(String message) {
        listener.getLogger().println("[ERROR] - " + message);
    }

}
