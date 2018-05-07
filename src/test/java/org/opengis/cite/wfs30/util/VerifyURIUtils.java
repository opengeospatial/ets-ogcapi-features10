package org.opengis.cite.wfs30.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the URIUtils class.
 */
public class VerifyURIUtils {

    public VerifyURIUtils() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @Ignore
    @Test
    // comment out @Ignore to run test (requires network connection)
    public void resolveHttpUriAsDocument() throws SAXException, IOException {
        URI uriRef = URI.create("http://www.w3schools.com/xml/note.xml");
        Document doc = URIUtils.parseURI(uriRef);
        Assert.assertNotNull(doc);
        Assert.assertEquals("Document element has unexpected [local name].",
                "note", doc.getDocumentElement().getLocalName());
    }

    @Ignore
    @Test
    // comment out @Ignore to run test (requires network connection)
    public void resolveHttpUriAsFile() throws SAXException, IOException {
        URI uriRef = URI.create("http://www.w3schools.com/xml/note.xml");
        File file = URIUtils.dereferenceURI(uriRef);
        Assert.assertNotNull(file);
        Assert.assertTrue("File should not be empty", file.length() > 0);
    }

    @Test
    public void resolveClasspathResource() throws SAXException, IOException,
            URISyntaxException {
        URL url = this.getClass().getResource("/atom-feed.xml");
        Document doc = URIUtils.parseURI(url.toURI());
        Assert.assertNotNull(doc);
        Assert.assertEquals("Document element has unexpected [local name].",
                "feed", doc.getDocumentElement().getLocalName());
    }

    @Test
    public void resolveFileRefWithXInclude() throws SAXException, IOException,
            URISyntaxException {
        File file = new File("src/test/resources/Alpha-xinclude.xml");
        Document doc = URIUtils.parseURI(file.toURI());
        Assert.assertNotNull(doc);
        Assert.assertEquals("Document element has unexpected [local name].",
                "Alpha", doc.getDocumentElement().getLocalName());
        NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(
                "http://www.example.net/gamma", "Gamma");
        Assert.assertEquals(
                "Expected element {http://www.example.net/gamma}Gamma", 1,
                nodes.getLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveMissingClasspathResource() throws SAXException,
            URISyntaxException, IOException {
        URL url = this.getClass().getResource("/alpha.xml");
        URI uri = (null != url) ? url.toURI() : null;
        Document doc = URIUtils.parseURI(uri);
        Assert.assertNull(doc);
    }
}
