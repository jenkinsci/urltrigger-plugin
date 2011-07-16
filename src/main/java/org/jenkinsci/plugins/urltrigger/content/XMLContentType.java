package org.jenkinsci.plugins.urltrigger.content;

import hudson.Extension;
import org.jenkinsci.plugins.urltrigger.URLTriggerException;
import org.jenkinsci.plugins.urltrigger.URLTriggerLog;
import org.kohsuke.stapler.DataBoundConstructor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory Boissinot
 */
public class XMLContentType extends URLTriggerContentType {

    private transient Map<String, Object> results;

    private transient Document xmlDocument;

    private List<XMLContentEntry> expressions = new ArrayList<XMLContentEntry>();

    @DataBoundConstructor
    public XMLContentType(List<XMLContentEntry> element) {
        if (element != null) {
            this.expressions = element;
        }
    }

    @SuppressWarnings("unused")
    public List<XMLContentEntry> getExpressions() {
        return expressions;
    }

    @Override
    public void initForContent(String content) throws URLTriggerException {
        xmlDocument = initXMLFile(content);
        results = readXMLPath(xmlDocument);
    }

    private Document initXMLFile(String content) throws URLTriggerException {
        Document xmlDocument;
        try {
            StringReader stringReader = new StringReader(content);
            InputSource inputSource = new InputSource(stringReader);
            xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);
            stringReader.close();
        } catch (SAXException e) {
            throw new URLTriggerException(e);
        } catch (IOException e) {
            throw new URLTriggerException(e);
        } catch (ParserConfigurationException e) {
            throw new URLTriggerException(e);
        }
        return xmlDocument;
    }

    private Map<String, Object> readXMLPath(Document document) throws URLTriggerException {
        Map<String, Object> results = new HashMap<String, Object>(expressions.size());
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        try {
            for (XMLContentEntry expressionEntry : expressions) {
                String expression = expressionEntry.getExpression();
                XPathExpression xPathExpression = xPath.compile(expression);
                Object result = xPathExpression.evaluate(document);
                results.put(expression, result);
            }
        } catch (XPathExpressionException xpe) {
            throw new URLTriggerException(xpe);
        }
        return results;
    }

    @Override
    public boolean isTriggeringBuildForContent(String content, URLTriggerLog log) throws URLTriggerException {

        Document newDocument = initXMLFile(content);
        Map<String, Object> newResults = readXMLPath(newDocument);

        if (results == null) {
            throw new NullPointerException("Initial result object must not be a null reference.");
        }
        if (newResults == null) {
            throw new NullPointerException("New computed results object must not be a null reference.");
        }

        if (results.size() != newResults.size()) {
            throw new URLTriggerException("Regarding the trigger life cycle, the size between old results and new results has to be the same.");
        }

        //The results object have to be the same keys
        if (!results.keySet().containsAll(newResults.keySet())) {
            throw new URLTriggerException("Regarding the set up of the result objects, the keys for the old results and the new results have to be the same.");
        }


        for (Map.Entry<String, Object> entry : results.entrySet()) {

            String expression = entry.getKey();
            Object initValue = entry.getValue();
            Object newValue = newResults.get(expression);

            if (initValue == null && newValue == null) {
                log.info(String.format("There is no matching for the expression '%s'.", expression));
                continue;
            }

            if (initValue == null && newValue != null) {
                log.info(String.format("There was no value and now there is a new value for the expression '%s'.", expression));
                return true;
            }

            if (initValue != null && newValue == null) {
                log.info(String.format("There was a value and now there is no value for the expression '%s'.", expression));
                return true;
            }

            if (!initValue.equals(newValue)) {
                log.info(String.format("The value for the expression '%s' has changed.", expression));
                return true;
            }

        }

        return false;
    }

    @Extension
    @SuppressWarnings("unused")
    public static class XMLContentDescriptor extends URLTriggerContentTypeDescriptor<XMLContentType> {

        @Override
        public Class<? extends URLTriggerContentType> getType() {
            return XMLContentType.class;
        }

        @Override
        public String getDisplayName() {
            return "Monitor the contents of an XML response";
        }

        @Override
        public String getLabel() {
            return "XML";
        }
    }

}
