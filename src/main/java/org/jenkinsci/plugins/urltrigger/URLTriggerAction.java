package org.jenkinsci.plugins.urltrigger;

import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.AbstractProject;
import hudson.model.Action;
import org.apache.commons.jelly.XMLOutput;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerAction implements Action {

    private transient AbstractProject<?, ?> job;
    private transient File logFile;
    private transient String label;
    private transient Map<String, String> subActionTitle;

    public URLTriggerAction(AbstractProject<?, ?> job, File logFile, String label, Map<String, String> subActionTitle) {
        this.job = job;
        this.logFile = logFile;
        this.label = label;
        this.subActionTitle = subActionTitle;
    }

    @SuppressWarnings("unused")
    public AbstractProject<?, ?> getOwner() {
        return job;
    }

    @SuppressWarnings("unused")
    public String getLabel() {
        return label;
    }

    @SuppressWarnings("unused")
    public String getIconFileName() {
        return "clipboard.gif";
    }

    public String getDisplayName() {
        return "URLTrigger Log";
    }

    @SuppressWarnings("unused")
    public String getUrlName() {
        return "urltriggerPollLog";
    }

    @SuppressWarnings("unused")
    public String getLog() throws IOException {
        return Util.loadFile(getLogFile());
    }

    @SuppressWarnings("unused")
    public Map<String, String> getSubActionTitle() {
        return subActionTitle;
    }

    public File getLogFile() {
        return logFile;
    }

    @SuppressWarnings("unused")
    public void writeLogTo(XMLOutput out) throws IOException {
        new AnnotatedLargeText<URLTriggerAction>(getLogFile(), Charset.defaultCharset(), true, this).writeHtmlTo(0, out.asWriter());
    }

}