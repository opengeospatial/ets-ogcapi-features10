package org.opengis.cite.wfs30.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.logging.Level;

import javax.ws.rs.core.HttpHeaders;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Provides a collection of utility methods for manipulating or resolving URI
 * references.
 */
public class URIUtils {

    private static final String FIXUP_BASE_URI = "http://apache.org/xml/features/xinclude/fixup-base-uris";

    /**
     * Parses the content of the given URI as an XML document and returns a new
     * DOM Document object. Entity reference nodes will not be expanded. XML
     * inclusions (xi:include elements) will be processed if present.
     * 
     * @param uriRef
     *            An absolute URI specifying the location of an XML resource.
     * @return A DOM Document node representing an XML resource.
     * @throws SAXException
     *             If the resource cannot be parsed.
     * @throws IOException
     *             If the resource is not accessible.
     */
    public static Document parseURI(URI uriRef) throws SAXException,
            IOException {
        if ((null == uriRef) || !uriRef.isAbsolute()) {
            throw new IllegalArgumentException(
                    "Absolute URI is required, but received " + uriRef);
        }
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        docFactory.setNamespaceAware(true);
        docFactory.setExpandEntityReferences(false);
        docFactory.setXIncludeAware(true);
        Document doc = null;
        try {
            // XInclude processor will not add xml:base attributes
            docFactory.setFeature(FIXUP_BASE_URI, false);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(uriRef.toString());
        } catch (ParserConfigurationException x) {
            TestSuiteLogger.log(Level.WARNING,
                    "Failed to create DocumentBuilder." + x);
        }
        if (null != doc) {
            doc.setDocumentURI(uriRef.toString());
        }
        return doc;
    }

    /**
     * Dereferences the given URI and stores the resulting resource
     * representation in a local file. The file will be located in the default
     * temporary file directory.
     * 
     * @param uriRef
     *            An absolute URI specifying the location of some resource.
     * @return A File containing the content of the resource; it may be empty if
     *         resolution failed for any reason.
     * @throws IOException
     *             If an IO error occurred.
     */
    public static File dereferenceURI(URI uriRef) throws IOException {
        if ((null == uriRef) || !uriRef.isAbsolute()) {
            throw new IllegalArgumentException(
                    "Absolute URI is required, but received " + uriRef);
        }
        if (uriRef.getScheme().equalsIgnoreCase("file")) {
            return new File(uriRef);
        }
        Client client = Client.create();
        WebResource webRes = client.resource(uriRef);
        ClientResponse rsp = webRes.get(ClientResponse.class);
        String suffix = null;
        if (rsp.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE).endsWith("xml")) {
            suffix = ".xml";
        }
        File destFile = File.createTempFile("entity-", suffix);
        if (rsp.hasEntity()) {
            InputStream is = rsp.getEntityInputStream();
            OutputStream os = new FileOutputStream(destFile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.flush();
            os.close();
        }
        TestSuiteLogger.log(Level.FINE, "Wrote " + destFile.length()
                + " bytes to file at " + destFile.getAbsolutePath());
        return destFile;
    }

    /**
     * Constructs an absolute URI from the given URI reference and a base URI.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3986#section-5.2">RFC 3986,
     *      5.2</a>
     * 
     * @param baseURI
     *            The base URI; if present, it must be an absolute URI.
     * @param uriRef
     *            A URI reference that may be relative to the given base URI.
     * @return The resulting URI.
     * 
     */
    public static URI resolveRelativeURI(String baseURI, String uriRef) {
        URI uri = (null != baseURI) ? URI.create(baseURI) : URI.create("");
        if (null != baseURI && null == uri.getScheme()) {
            throw new IllegalArgumentException(
                    "Base URI has no scheme component: " + baseURI);
        }
        return uri.resolve(uriRef);
    }
}
