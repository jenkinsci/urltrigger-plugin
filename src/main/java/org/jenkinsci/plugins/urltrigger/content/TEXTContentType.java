package org.jenkinsci.plugins.urltrigger.content;

import hudson.Extension;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.URLTriggerLog;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gregory Boissinot
 */
public class TEXTContentType extends URLTriggerContentType {

    private List<TEXTContentEntry> regExElements = new ArrayList<TEXTContentEntry>();

    @DataBoundConstructor
    public TEXTContentType(List<TEXTContentEntry> element) {
        if (element != null) {
            this.regExElements = element;
        }
    }

    public List<TEXTContentEntry> getRegExElements() {
        return regExElements;
    }

    @Override
    public void initForContentType(String content) throws URLTriggerException {
    }

    @Override
    public boolean isTriggeringBuildForContent(String content, URLTriggerLog log) throws URLTriggerException {

        StringReader stringReader = null;
        BufferedReader bufferedReader = null;
        try {
            stringReader = new StringReader(content);
            bufferedReader = new BufferedReader(stringReader);
            String line;

            //Check line by line if a pattern matches
            while ((line = bufferedReader.readLine()) != null) {
                for (TEXTContentEntry regexEntry : regExElements) {
                    Pattern pattern = Pattern.compile(regexEntry.getRegEx());
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        log.info(String.format("The line '%s' matches the pattern '%s'", line, pattern));
                        return true;
                    }
                }
            }
            return false;
        } catch (IOException ioe) {
            throw new URLTriggerException(ioe);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioe) {
                    throw new URLTriggerException(ioe);
                }
            }
            if (stringReader != null) {
                stringReader.close();
            }
        }
    }

    @Extension
    @SuppressWarnings("unused")
    public static class TEXTContentDescriptor extends URLTriggerContentTypeDescriptor<TEXTContentType> {

        @Override
        public Class<? extends URLTriggerContentType> getType() {
            return TEXTContentType.class;
        }

        @Override
        public String getDisplayName() {
            return "Monitor the contents of a TEXT response";
        }

        @Override
        public String getLabel() {
            return "TXT";
        }
    }

}
