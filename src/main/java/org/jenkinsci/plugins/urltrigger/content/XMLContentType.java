package org.jenkinsci.plugins.urltrigger.content;

import hudson.Extension;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
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

    private transient Map<String, Object> results = new HashMap<String, Object>();

    private transient Document xmlDocument;

    private List<XMLContentEntry> xPaths = new ArrayList<XMLContentEntry>();

    @DataBoundConstructor
    public XMLContentType(List<XMLContentEntry> element) {
        if (element != null) {
            this.xPaths = element;
        }
    }

    @SuppressWarnings("unused")
    public List<XMLContentEntry> getXPaths() {
        return xPaths;
    }

    @Override
    protected void initForContentType(String content, XTriggerLog log) throws XTriggerException {
        xmlDocument = initXMLFile(content);
        results = readXMLPath(xmlDocument);
    }

    private Document initXMLFile(String content) throws XTriggerException {
        Document xmlDocument;
        try {
            StringReader stringReader = new StringReader(content);
            InputSource inputSource = new InputSource(stringReader);
            xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);
            stringReader.close();
        } catch (SAXException e) {
            throw new XTriggerException(e);
        } catch (IOException e) {
            throw new XTriggerException(e);
        } catch (ParserConfigurationException e) {
            throw new XTriggerException(e);
        }
        return xmlDocument;
    }

    private Map<String, Object> readXMLPath(Document document) throws XTriggerException {
        Map<String, Object> results = new HashMap<String, Object>(xPaths.size());
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        try {
            for (XMLContentEntry expressionEntry : xPaths) {
                String expression = expressionEntry.getXPath();
                XPathExpression xPathExpression = xPath.compile(expression);
                Object result = xPathExpression.evaluate(document);
                results.put(expression, result);
            }
        } catch (XPathExpressionException xpe) {
            throw new XTriggerException(xpe);
        }
        return results;
    }

    @Override
    protected boolean isTriggeringBuildForContent(String content, XTriggerLog log) throws XTriggerException {

        if (results == null) {
            log.info("Capturing URL context. Waiting next schedule to check a change.");
            return false;
        }

        Document newDocument = initXMLFile(content);
        Map<String, Object> newResults = readXMLPath(newDocument);

        if (newResults == null) {
            throw new NullPointerException("New computed results object must not be a null reference.");
        }

        if (results.size() != newResults.size()) {
            throw new XTriggerException("The size between old results and new results has to be the same.");
        }

        //The results object have to be the same keys
        if (!results.keySet().containsAll(newResults.keySet())) {
            throw new XTriggerException("According the setup of the result objects, the keys for the old results and the new results have to be the same.");
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
