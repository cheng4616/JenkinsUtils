package payment.tools.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * <pre>
 * Modify Information:
 * Author       Date          Description
 * ============ ============= ============================
 * liuzhicheng      2017年12月18日       create this files
 * </pre>
 */
public class XmlUtil {

    // Create a Schema from a file.
    public static Schema createSchema(File schemaFile) throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return schemaFactory.newSchema(schemaFile);
    }

    // Validate the document with the schema via DOMSource.
    public static void validateViaDOM(Document document, Schema schema) throws Exception {
        DOMSource source = new DOMSource(document);
        Validator validator = schema.newValidator();
        validator.validate(source);
    }

    // Validate the document with the schema via DOMSource.
    public static void validateViaDOM(String xmlString, Schema schema) throws Exception {
        DOMSource source = new DOMSource(createDocument(xmlString));
        Validator validator = schema.newValidator();
        validator.validate(source);
    }

    // Validate the document with the schema via SAXSource.
    public static void validateViaSAX(String xmlString, Schema schema) throws Exception {
        SAXSource source = new SAXSource(new InputSource(new InputStreamReader(new ByteArrayInputStream(xmlString.getBytes(StringUtil.DEFAULT_CHARSET)))));
        Validator validator = schema.newValidator();
        validator.validate(source);
    }

    // Validate the document with the schema via StreamSource.
    public static void validateViaStreamSource(String xmlString, Schema schema) throws Exception {
        StreamSource source = new StreamSource(new ByteArrayInputStream(xmlString.getBytes(StringUtil.DEFAULT_CHARSET)));
        Validator validator = schema.newValidator();
        validator.validate(source);
    }

    // Create a Document from a String.
    public static Document createDocument(String xmlString) throws Exception {
        DocumentBuilder documentBuilder = createDocumentBuilder();
        return documentBuilder.parse(new ByteArrayInputStream(xmlString.getBytes(StringUtil.DEFAULT_CHARSET)));
    }

    // Create a Document from a String.
    public static Document createDocument(String xmlString, String charset) throws Exception {
        DocumentBuilder documentBuilder = createDocumentBuilder();
        return documentBuilder.parse(new ByteArrayInputStream(xmlString.getBytes(charset)));
    }

    // Create a Document from a File.
    public static Document createDocument(File xmlFile) throws Exception {
        DocumentBuilder documentBuilder = createDocumentBuilder();
        return documentBuilder.parse(xmlFile);
    }

    // Create a DocumentBuilder
    public static DocumentBuilder createDocumentBuilder() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        return documentBuilderFactory.newDocumentBuilder();
    }

    // Get the node'text from the document.
    public static String getNodeText(Document document, String nodeName) throws Exception {
        return getNodeText(document, nodeName, 0);
    }

    // Get the node'text from the document.
    public static String getNodeText(Document document, String nodeName, int index) throws Exception {
        NodeList nodeList = document.getElementsByTagName(nodeName);
        if (nodeList == null || index >= nodeList.getLength()) {
            return null;
        } else {
            return nodeList.item(index).getTextContent();
        }
    }

    public static String getChildNodeText(Node node, String childName) {
        NodeList children = node.getChildNodes();
        int len = children.getLength();
        for (int i = 0; i < len; i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName() == childName) {
                return child.getTextContent();
            }
        }
        return null;
    }

    // Get Node List
    public static List<String> getNodeList(Document document, String nodeName) throws Exception {
        NodeList nodeList = document.getElementsByTagName(nodeName);
        List<String> textList = new ArrayList<String>();
        if (nodeList == null) {
            return null;
        } else {
            for (int i = 0; i < nodeList.getLength(); i++) {
                textList.add(nodeList.item(i).getTextContent());
            }
            return textList;
        }
    }

    // Get the attribute's value of the node from the document.
    public static String getNodeAttributeValue(Document document, String nodeName, String attributeName) throws Exception {
        NodeList nodeList = document.getElementsByTagName(nodeName);
        if (nodeList != null && nodeList.getLength() > 0) {
            Element element = (Element) nodeList.item(0);
            return element.getAttribute(attributeName);
        }
        return null;
    }

    public static String getNodeText(Node node, String childName) throws Exception {
        NodeList children = node.getChildNodes();
        int len = children.getLength();
        for (int i = 0; i < len; i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(childName)) {
                return child.getTextContent();
            }
        }
        return null;
    }

    public static Node getChildNode(Node node, String childName) throws Exception {
        NodeList children = node.getChildNodes();
        int len = children.getLength();
        for (int i = 0; i < len; i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(childName)) {
                return child;
            }
        }
        return null;
    }

    // Convert a Document to a String with pretty format.
    public static String createPrettyFormat(Document document) throws Exception {
        return createPrettyFormat(document, StringUtil.DEFAULT_CHARSET);
    }

    public static String createPrettyFormat(Document document, String outputCharset) throws Exception {
        Source source = new DOMSource(document);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, outputCharset);
        Writer writer = new StringWriter();
        transformer.transform(source, new StreamResult(writer));
        return writer.toString();
    }

    public static String createCompactFormat(Document document) throws Exception {
        return createCompactFormat(document, StringUtil.DEFAULT_CHARSET);
    }

    public static String createCompactFormat(Document document, String outputCharset) throws Exception {
        Source source = new DOMSource(document);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, outputCharset);
        Writer writer = new StringWriter();
        transformer.transform(source, new StreamResult(writer));
        return writer.toString();
    }

}
