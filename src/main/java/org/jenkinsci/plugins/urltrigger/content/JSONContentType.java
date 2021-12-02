package org.jenkinsci.plugins.urltrigger.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.Symbol;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.urltrigger.content.json.util.JsonUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import hudson.Extension;

/**
 * @author Gregory Boissinot
 */
public class JSONContentType extends URLTriggerContentType {

	private static final long serialVersionUID = 9089691686677107132L;

	private transient Map<String, Object> results = null;

	private List<JSONContentEntry> jsonPaths = new ArrayList<>();

	@DataBoundConstructor
	public JSONContentType(List<JSONContentEntry> jsonPaths) {
		if (jsonPaths != null) {
			this.jsonPaths = jsonPaths;
		}
	}

	@SuppressWarnings("unused")
	public List<JSONContentEntry> getJsonPaths() {
		return jsonPaths;
	}

	@Override
	protected void initForContentType(String content, XTriggerLog log) throws XTriggerException {
		try {
			JsonUtils.validateJson(content);
			results = readJsonPath(content);
		} catch (XTriggerException pe) {
			log.error("An error occurred when parsing the document - may not be valid JSON?");
			throw pe;
		}
	}

	private Map<String, Object> readJsonPath(String content) throws XTriggerException {
		Map<String, Object> results = new HashMap<>(jsonPaths.size());
		for (JSONContentEntry jsonContentEntry : jsonPaths) {
			String jsonPath = jsonContentEntry.getJsonPath();
			try {
				Object result = JsonPath.read(content, jsonPath);
				results.put(jsonPath, result);
			} catch (PathNotFoundException pe) {
				// As suggested by Tony Noble, just apply null to the jsonPath result
				// to enable the mechanism to detect jsonPath could not be found in the document.
				results.put(jsonPath, null); 
			}
		}
		return results;
	}

	@Override
	public Map<String, String> getTriggeringResponse() {
		Map<String, String> payload = new HashMap<>();
		if (results != null) {
			results.forEach((key, value) -> payload.put(key, value.toString()));
		}
		return payload;
	}

	@Override
	protected boolean isTriggeringBuildForContent(String content, XTriggerLog log) throws XTriggerException {

		if (jsonPaths == null || jsonPaths.size() == 0) {
			log.error("You must configure at least one JSON Path. Exit with no changes.");
			return false;
		}

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

		// The results object have to be the same keys
		if (!results.keySet().containsAll(newResults.keySet())) {
			throw new XTriggerException(
					"Regarding the set up of the result objects, the keys for the old results and the new results have to be the same.");
		}

		for (Map.Entry<String, Object> entry : results.entrySet()) {
			String jsonPath = entry.getKey();
			Object initValue = entry.getValue();
			Object newValue = newResults.get(jsonPath);

			boolean initValueNull = (initValue == null);
			boolean newValueNull = (newValue == null);

			if (initValueNull && newValueNull) {
				log.info(String.format("There is no matching for the JSON Path '%s'.", jsonPath));
				continue;
			} else if (initValueNull && !newValueNull) {
				log.info(String.format("There was no value and now there is a new value for the JSON Path '%s'.",
						jsonPath));
				return true;
			} else if (!initValueNull && newValueNull) {
				log.info(
						String.format("There was a value and now there is no value for the JSON Path '%s'.", jsonPath));
				return true;
			} else if (!initValue.equals(newValue)) {
				log.info(String.format("The value for the JSON Path '%s' has changed.", jsonPath));
				return true;
			}
		}

		return false;
	}

	@Extension
	@SuppressWarnings("unused")
	@Symbol("JsonContent")
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
