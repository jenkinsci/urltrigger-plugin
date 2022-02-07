package org.jenkinsci.plugins.urltrigger.content.json.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.jenkinsci.plugins.xtriggerapi.XTriggerException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Gregory Boissinot
 */
public class JsonUtils {

    public static void validateJson(String content) throws XTriggerException {
        try {
            JsonParser parser = new JsonFactory().createParser(content.getBytes(StandardCharsets.UTF_8));
            JsonToken currentToken = parser.nextToken();
            if (currentToken.equals(JsonToken.START_OBJECT)) {
                validateObject(parser);
            } else if (currentToken.equals(JsonToken.START_ARRAY)) {
                validateArray(parser);
            } else {
                throw new XTriggerException("Bad Json value starting with: " + currentToken);
            }
        } catch (IOException ex) {
            throw new XTriggerException("Bad Json value: " + ex.getMessage());
        }
    }

    private static void validateObject(JsonParser parser) throws IOException {
        JsonToken currentToken = parser.nextValue();
        while (currentToken != null && !currentToken.equals(JsonToken.END_OBJECT)) {
            if (currentToken.toString().startsWith("VALUE_")) {
                currentToken = parser.nextValue();
            } else if (currentToken.equals(JsonToken.START_ARRAY)) {
                validateArray(parser);
                currentToken = parser.nextValue();
            } else if (currentToken.equals(JsonToken.START_OBJECT)) {
                validateObject(parser);
                currentToken = parser.nextValue();
            } else {
                throw new IOException("Expected object/array start, found: " + currentToken);
            }
        }
    }

    private static void validateArray(JsonParser parser) throws IOException {
        JsonToken currentToken = parser.nextValue();
        while (currentToken != null && !currentToken.equals(JsonToken.END_ARRAY)) {
            if (currentToken.toString().startsWith("VALUE_")) {
                currentToken = parser.nextValue();
            } else if (currentToken.equals(JsonToken.START_ARRAY)) {
                validateArray(parser);
                currentToken = parser.nextValue();
            } else if (currentToken.equals(JsonToken.START_OBJECT)) {
                validateObject(parser);
                currentToken = parser.nextValue();
            } else {
                throw new IOException("Expected object/array start, found: " + currentToken);
            }
        }
    }

}
