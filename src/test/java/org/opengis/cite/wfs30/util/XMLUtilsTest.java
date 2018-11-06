package org.opengis.cite.wfs30.util;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertTrue;

/**
 * Verifies the behavior of the XMLUtils class.
 */
public class XMLUtilsTest {

    private static DocumentBuilder docBuilder;

    @BeforeClass
    public static void setUpClass()
                            throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void writeDocToString()
                            throws SAXException, IOException {
        Document doc = docBuilder.parse( this.getClass().getResourceAsStream( "../landingPage.xml" ) );
        String content = XMLUtils.writeNodeToString( doc );
        assertTrue( "String should start with '<LandingPage'", content.startsWith( "<LandingPage" ) );
    }

}
