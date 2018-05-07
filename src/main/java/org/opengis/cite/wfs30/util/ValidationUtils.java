package org.opengis.cite.wfs30.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.util.XMLCatalogResolver;
import org.opengis.cite.wfs30.Namespaces;
import org.opengis.cite.validation.SchematronValidator;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * A utility class that provides convenience methods to support schema
 * validation.
 */
public class ValidationUtils {

    static final String ROOT_PKG = "/org/opengis/cite/wfs30/";
    private static final XMLCatalogResolver SCH_RESOLVER = initCatalogResolver();

    private static XMLCatalogResolver initCatalogResolver() {
        return (XMLCatalogResolver) createSchemaResolver(Namespaces.SCH);
    }

    /**
     * Creates a resource resolver suitable for locating schemas using an entity
     * catalog. In effect, local copies of standard schemas are returned instead
     * of retrieving them from external repositories.
     * 
     * @param schemaLanguage
     *            A URI that identifies a schema language by namespace name.
     * @return A {@code LSResourceResolver} object that is configured to use an
     *         OASIS entity catalog.
     */
    public static LSResourceResolver createSchemaResolver(URI schemaLanguage) {
        String catalogFileName;
        if (schemaLanguage.equals(Namespaces.XSD)) {
            catalogFileName = "schema-catalog.xml";
        } else {
            catalogFileName = "schematron-catalog.xml";
        }
        URL catalogURL = ValidationUtils.class.getResource(ROOT_PKG
                + catalogFileName);
        XMLCatalogResolver resolver = new XMLCatalogResolver();
        resolver.setCatalogList(new String[] { catalogURL.toString() });
        return resolver;
    }

    /**
     * Constructs a SchematronValidator that will check an XML resource against
     * the rules defined in a Schematron schema. An attempt is made to resolve
     * the schema reference using an entity catalog; if this fails the reference
     * is used as given.
     * 
     * @param schemaRef
     *            A reference to a Schematron schema; this is expected to be a
     *            relative or absolute URI value, possibly matching the system
     *            identifier for some entry in an entity catalog.
     * @param phase
     *            The name of the phase to invoke.
     * @return A SchematronValidator instance, or {@code null} if the validator
     *         cannot be constructed (e.g. invalid schema reference or phase
     *         name).
     */
    public static SchematronValidator buildSchematronValidator(
            String schemaRef, String phase) {
        Source source = null;
        try {
            String catalogRef = SCH_RESOLVER
                    .resolveSystem(schemaRef.toString());
            if (null != catalogRef) {
                source = new StreamSource(URI.create(catalogRef).toString());
            } else {
                source = new StreamSource(schemaRef);
            }
        } catch (IOException x) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error reading Schematron schema catalog.", x);
        }
        SchematronValidator validator = null;
        try {
            validator = new SchematronValidator(source, phase);
        } catch (Exception e) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error creating Schematron validator.", e);
        }
        return validator;
    }

    /**
     * Extracts a set of XML Schema references from a source XML document. The
     * document element is expected to include the standard xsi:schemaLocation
     * attribute.
     * 
     * @param source
     *            The source instance to read from; its base URI (systemId)
     *            should be set.
     * @param baseURI
     *            An alternative base URI to use if the source does not have a
     *            system identifier set or if its system id is a {@code file}
     *            URI. This will usually be the URI used to retrieve the
     *            resource; it may be null.
     * @return A Set containing absolute URI references that specify the
     *         locations of XML Schema resources.
     * @throws XMLStreamException
     *             If an error occurs while reading the source instance.
     */
    public static Set<URI> extractSchemaReferences(Source source, String baseURI)
            throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = factory.createXMLEventReader(source);
        // advance to document element
        StartElement docElem = reader.nextTag().asStartElement();
        Attribute schemaLoc = docElem.getAttributeByName(new QName(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"));
        if (null == schemaLoc) {
            throw new RuntimeException(
                    "No xsi:schemaLocation attribute found. See ISO 19136, A.3.1.");
        }
        String[] uriValues = schemaLoc.getValue().split("\\s+");
        if (uriValues.length % 2 != 0) {
            throw new RuntimeException(
                    "xsi:schemaLocation attribute contains an odd number of URI values:\n"
                            + Arrays.toString(uriValues));
        }
        Set<URI> schemaURIs = new HashSet<URI>();
        // one or more pairs of [namespace name] [schema location]
        for (int i = 0; i < uriValues.length; i += 2) {
            URI schemaURI = null;
            if (!URI.create(uriValues[i + 1]).isAbsolute()
                    && (null != source.getSystemId())) {
                String schemaRef = URIUtils.resolveRelativeURI(
                        source.getSystemId(), uriValues[i + 1]).toString();
                if (schemaRef.startsWith("file")
                        && !new File(schemaRef).exists() && (null != baseURI)) {
                    schemaRef = URIUtils.resolveRelativeURI(baseURI,
                            uriValues[i + 1]).toString();
                }
                schemaURI = URI.create(schemaRef);
            } else {
                schemaURI = URI.create(uriValues[i + 1]);
            }
            schemaURIs.add(schemaURI);
        }
        return schemaURIs;
    }
}
