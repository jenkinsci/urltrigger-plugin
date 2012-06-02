package org.jenkinsci.plugins.urltrigger.content;

import com.jayway.jsonpath.JsonPath;
import hudson.Extension;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.urltrigger.content.json.util.JsonUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory Boissinot
 */
public class JSONContentType extends URLTriggerContentType {

    private transient Map<String, Object> results = new HashMap<String, Object>();

    private List<JSONContentEntry> jsonPaths = new ArrayList<JSONContentEntry>();

    @DataBoundConstructor
    public JSONContentType(List<JSONContentEntry> element) {
        if (element != null) {
            this.jsonPaths = element;
        }
    }

    @SuppressWarnings("unused")
    public List<JSONContentEntry> getJsonPaths() {
        return jsonPaths;
    }

    @Override
    protected void initForContentType(String content, XTriggerLog log) throws XTriggerException {
        JsonUtils.validateJson(content);
        results = readJsonPath(content);
    }

    private Map<String, Object> readJsonPath(String content) throws XTriggerException {
        Map<String, Object> results = new HashMap<String, Object>(jsonPaths.size());
        try {
            for (JSONContentEntry jsonContentEntry : jsonPaths) {
                String jsonPath = jsonContentEntry.getJsonPath();
                Object result = JsonPath.read(content, jsonPath);
                results.put(jsonPath, result);
            }
        } catch (ParseException pe) {
            throw new XTriggerException(pe);
        }
        return results;
    }

    @Override
    protected boolean isTriggeringBuildForContent(String content, XTriggerLog log) throws XTriggerException {

        if (results == null) {
            log.info("Capturing URL context. Waiting next schedule to check a change.");
            return false;
        }

        Map<String, Object> newResults = readJsonPath(content);

        if (newResults == null) {
            throw new NullPointerException("New computed results object must not be a null reference.");
        }

        if (results.size() != newResults.size()) {
            throw new XTriggerException("The size between old results and new results has to be the same.");
        }

        //The results object have to be the same keys
        if (!results.keySet().containsAll(newResults.keySet())) {
            throw new XTriggerException("Regarding the set up of the result objects, the keys for the old results and the new results have to be the same.");
        }

        for (Map.Entry<String, Object> entry : results.entrySet()) {

            String jsonPath = entry.getKey();
            Object initValue = entry.getValue();
            Object newValue = newResults.get(jsonPath);

            if (initValue == null && newValue == null) {
                log.info(String.format("There is no matching for the JSON Path '%s'.", jsonPath));
                continue;
            }

            if (initValue == null && newValue != null) {
                log.info(String.format("There was no value and now there is a new value for the JSON Path '%s'.", jsonPath));
                return true;
            }

            if (initValue != null && newValue == null) {
                log.info(String.format("There was a value and now there is no value for the JSON Path '%s'.", jsonPath));
                return true;
            }

            if (!initValue.equals(newValue)) {
                log.info(String.format("The value for the JSON Path '%s' has changed.", jsonPath));
                return true;
            }
        }

        return false;
    }

    @Extension
    @SuppressWarnings("unused")
    public static class JSONContentDescriptor extends URLTriggerContentTypeDescriptor<XMLContentType> {

        @Override
        public Class<? extends URLTriggerContentType> getType() {
            return JSONContentType.class;
        }

        @Override
        public String getDisplayName() {
            return "Monitor the contents of a JSON path";
        }

        @Override
        public String getLabel() {
            return "JSON";
        }
    }

}
