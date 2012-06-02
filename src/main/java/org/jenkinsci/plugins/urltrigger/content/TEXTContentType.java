package org.jenkinsci.plugins.urltrigger.content;

import hudson.Extension;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gregory Boissinot
 */
public class TEXTContentType extends URLTriggerContentType {

    private List<TEXTContentEntry> regExElements = new ArrayList<TEXTContentEntry>();

    private transient Map<String, List<String>> capturedValues;

    @DataBoundConstructor
    public TEXTContentType(List<TEXTContentEntry> element) {
        if (element != null) {
            this.regExElements = element;
        }
    }

    @SuppressWarnings("unused")
    public List<TEXTContentEntry> getRegExElements() {
        return regExElements;
    }

    @Override
    protected void initForContentType(String content, XTriggerLog log) throws XTriggerException {
        capturedValues = getMatchedValue(content);
    }

    @Override
    protected boolean isTriggeringBuildForContent(String content, XTriggerLog log) throws XTriggerException {

        if (capturedValues == null) {
            log.info("Capturing URL context. Waiting next schedule to check a change.");
            return false;
        }

        Map<String, List<String>> newCapturedValues = getMatchedValue(content);

        if (capturedValues.size() != newCapturedValues.size()) {
            log.info("There are less or more matching elements.");
            return true;
        }

        for (Map.Entry<String, List<String>> entry : capturedValues.entrySet()) {

            String regEx = entry.getKey();
            if (!newCapturedValues.containsKey(regEx)) {
                log.info(String.format("The regular expression %s doesn't exist anymore.", regEx));
                return true;
            }

            List<String> oldValues = entry.getValue();
            List<String> newValues = newCapturedValues.get(entry.getKey());

            if (oldValues == null) {
                return false;
            }

            if (newValues == null) {
                return false;
            }

            if (newValues.size() != oldValues.size()) {
                log.info(String.format("The number of values for the regular expression %s is different.", regEx));
                return true;
            }

            for (String oldValue : oldValues) {
                if (!newValues.contains(oldValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, List<String>> getMatchedValue(String content) throws XTriggerException {

        Map<String, List<String>> capturedValues = new HashMap<String, List<String>>();

        StringReader stringReader = null;
        BufferedReader bufferedReader = null;
        try {
            stringReader = new StringReader(content);
            bufferedReader = new BufferedReader(stringReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                for (TEXTContentEntry regexEntry : regExElements) {
                    String regEx = regexEntry.getRegEx();
                    Pattern pattern = Pattern.compile(regEx);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        addMatchedValue(capturedValues, regEx, matcher.group());
                    }
                }
            }
        } catch (IOException ioe) {
            throw new XTriggerException(ioe);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioe) {
                    throw new XTriggerException(ioe);
                }
            }
            if (stringReader != null) {
                stringReader.close();
            }
        }
        return capturedValues;
    }

    private Map<String, List<String>> addMatchedValue(Map<String, List<String>> capturedValues, String regEx, String group) {
        List<String> values = capturedValues.get(regEx);
        if (values == null) {
            values = new ArrayList<String>();
            values.add(group);
            capturedValues.put(regEx, values);
            return capturedValues;
        }

        values.add(group);
        return capturedValues;
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
