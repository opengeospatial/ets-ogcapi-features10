package org.opengis.cite.wfs30;

import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.wfs30.util.NamespaceBindings;
import org.opengis.cite.wfs30.util.XMLUtils;
import org.opengis.cite.validation.SchematronValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides a set of custom assertion methods.
 */
public class ETSAssert {

    private static final Logger LOGR = Logger.getLogger(ETSAssert.class.getPackage().getName());

    private ETSAssert() {
    }

    /**
     * Asserts that the qualified name of a DOM Node matches the expected value.
     * 
     * @param node
     *            The Node to check.
     * @param qName
     *            A QName object containing a namespace name (URI) and a local
     *            part.
     */
    public static void assertQualifiedName(Node node, QName qName) {
        Assert.assertEquals(node.getLocalName(), qName.getLocalPart(), ErrorMessage.get(ErrorMessageKeys.LOCAL_NAME));
        Assert.assertEquals(node.getNamespaceURI(), qName.getNamespaceURI(),
                ErrorMessage.get(ErrorMessageKeys.NAMESPACE_NAME));
    }

    /**
     * Asserts that an XPath 1.0 expression holds true for the given evaluation
     * context. The following standard namespace bindings do not need to be
     * explicitly declared:
     * 
     * <ul>
     * <li>ows: {@value org.opengis.cite.wfs30.Namespaces#OWS}</li>
     * <li>xlink: {@value org.opengis.cite.wfs30.Namespaces#XLINK}</li>
     * <li>gml: {@value org.opengis.cite.wfs30.Namespaces#GML}</li>
     * </ul>
     * 
     * @param expr
     *            A valid XPath 1.0 expression.
     * @param context
     *            The context node.
     * @param namespaceBindings
     *            A collection of namespace bindings for the XPath expression,
     *            where each entry maps a namespace URI (key) to a prefix
     *            (value). It may be {@code null}.
     */
    public static void assertXPath(String expr, Node context, Map<String, String> namespaceBindings) {
        if (null == context) {
            throw new NullPointerException("Context node is null.");
        }
        NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
        bindings.addAllBindings(namespaceBindings);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(bindings);
        Boolean result;
        try {
            result = (Boolean) xpath.evaluate(expr, context, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException xpe) {
            String msg = ErrorMessage.format(ErrorMessageKeys.XPATH_ERROR, expr);
            LOGR.log(Level.WARNING, msg, xpe);
            throw new AssertionError(msg);
        }
        Element elemNode;
        if (Document.class.isInstance(context)) {
            elemNode = Document.class.cast(context).getDocumentElement();
        } else {
            elemNode = (Element) context;
        }
        Assert.assertTrue(result, ErrorMessage.format(ErrorMessageKeys.XPATH_RESULT, elemNode.getNodeName(), expr));
    }

    /**
     * Asserts that an XML resource is schema-valid.
     * 
     * @param validator
     *            The Validator to use.
     * @param source
     *            The XML Source to be validated.
     */
    public static void assertSchemaValid(Validator validator, Source source) {
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        validator.setErrorHandler(errHandler);
        try {
            validator.validate(source);
        } catch (Exception e) {
            throw new AssertionError(ErrorMessage.format(ErrorMessageKeys.XML_ERROR, e.getMessage()));
        }
        Assert.assertFalse(errHandler.errorsDetected(), ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
                errHandler.getErrorCount(), errHandler.toString()));
    }

    /**
     * Asserts that an XML resource satisfies all applicable constraints defined
     * for the specified phase in a Schematron (ISO 19757-3) schema. The "xslt2"
     * query language binding is supported. Two phase names have special
     * meanings:
     * <ul>
     * <li>"#ALL": All patterns are active</li>
     * <li>"#DEFAULT": The phase identified by the defaultPhase attribute on the
     * schema element should be used.</li>
     * </ul>
     * 
     * @param schemaRef
     *            A URL that denotes the location of a Schematron schema.
     * @param xmlSource
     *            The XML Source to be validated.
     * @param activePhase
     *            The active phase (pattern set) whose patterns are used for
     *            validation; this is set to "#ALL" if not specified.
     */
    public static void assertSchematronValid(URL schemaRef, Source xmlSource, String activePhase) {
        String phase = (null == activePhase || activePhase.isEmpty()) ? "#ALL" : activePhase;
        SchematronValidator validator;
        try {
            validator = new SchematronValidator(new StreamSource(schemaRef.toString()), phase);
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("Failed to process Schematron schema at ");
            msg.append(schemaRef).append('\n');
            msg.append(e.getMessage());
            throw new AssertionError(msg);
        }
        DOMResult result = validator.validate(xmlSource);
        Assert.assertFalse(validator.ruleViolationsDetected(), ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
                validator.getRuleViolationCount(), XMLUtils.writeNodeToString(result.getNode())));
    }

    /**
     * Asserts that the given XML entity contains the expected number of
     * descendant elements having the specified name.
     * 
     * @param xmlEntity
     *            A Document representing an XML entity.
     * @param elementName
     *            The qualified name of the element.
     * @param expectedCount
     *            The expected number of occurrences.
     */
    public static void assertDescendantElementCount(Document xmlEntity, QName elementName, int expectedCount) {
        NodeList features = xmlEntity.getElementsByTagNameNS(elementName.getNamespaceURI(), elementName.getLocalPart());
        Assert.assertEquals(features.getLength(), expectedCount,
                String.format("Unexpected number of %s descendant elements.", elementName));
    }

    /**
     * Asserts that the given response message contains an OGC exception report.
     * The message body must contain an XML document that has a document element
     * with the following properties:
     *
     * <ul>
     * <li>[local name] = "ExceptionReport"</li>
     * <li>[namespace name] = "http://www.opengis.net/ows/2.0"</li>
     * </ul>
     *
     * @param rsp
     *            A ClientResponse object representing an HTTP response message.
     * @param exceptionCode
     *            The expected OGC exception code.
     * @param locator
     *            A case-insensitive string value expected to occur in the
     *            locator attribute (e.g. a parameter name); the attribute value
     *            will be ignored if the argument is null or empty.
     */
    public static void assertExceptionReport(ClientResponse rsp, String exceptionCode, String locator) {
        Assert.assertEquals(rsp.getStatus(), ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document doc = rsp.getEntity(Document.class);
        String expr = String.format("//ows:Exception[@exceptionCode = '%s']", exceptionCode);
        NodeList nodeList = null;
        try {
            nodeList = XMLUtils.evaluateXPath(doc, expr, null);
        } catch (XPathExpressionException xpe) {
            // won't happen
        }
        Assert.assertTrue(nodeList.getLength() > 0, "Exception not found in response: " + expr);
        if (null != locator && !locator.isEmpty()) {
            Element exception = (Element) nodeList.item(0);
            String locatorValue = exception.getAttribute("locator").toLowerCase();
            Assert.assertTrue(locatorValue.contains(locator.toLowerCase()),
                    String.format("Expected locator attribute to contain '%s']", locator));
        }
    }
}
