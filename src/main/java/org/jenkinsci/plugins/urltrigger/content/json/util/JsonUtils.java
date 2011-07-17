package org.jenkinsci.plugins.urltrigger.content.json.util;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;

import java.io.IOException;

/**
 * @author Gregory Boissinot
 */
public class JsonUtils {

    public static void validateJson(String content) throws URLTriggerException {
        try {
            JsonParser parser = new JsonFactory().createJsonParser(content.getBytes());
            JsonToken currentToken = parser.nextToken();
            if (currentToken.equals(JsonToken.START_OBJECT)) {
                validateObject(parser);
            } else if (currentToken.equals(JsonToken.START_ARRAY)) {
                throw new URLTriggerException("Json documents starting with arrays are not supported!");
            } else {
                throw new URLTriggerException("Bad Json value starting with: " + currentToken.toString());
            }
        } catch (IOException ex) {
            throw new URLTriggerException("Bad Json value: " + ex.getMessage());
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
                throw new IOException("Expected object/array start, found: " + currentToken.toString());
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
                throw new IOException("Expected object/array start, found: " + currentToken.toString());
            }
        }
    }

}
