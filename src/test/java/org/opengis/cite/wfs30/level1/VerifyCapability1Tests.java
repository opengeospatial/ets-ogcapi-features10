package org.opengis.cite.wfs30.level1;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the Capability1Tests test class. Test stubs replace
 * fixture constituents where appropriate.
 */
public class VerifyCapability1Tests {

    private static final String SUBJ = "testSubject";
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;

    public VerifyCapability1Tests() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test(expected = AssertionError.class)
    public void testIsEmpty() {
        Capability1Tests iut = new Capability1Tests();
        iut.isEmpty();
    }

    @Test
    public void testTrim() {
        Capability1Tests iut = new Capability1Tests();
        iut.trim();
    }

    @Test(expected = NullPointerException.class)
    public void supplyNullTestSubject() throws SAXException, IOException {
        Capability1Tests iut = new Capability1Tests();
        iut.docIsValidAtomFeed();
    }

    @Test
    public void supplyValidAtomFeedViaTestContext() throws SAXException,
            IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/atom-feed.xml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        Capability1Tests iut = new Capability1Tests();
        iut.obtainTestSubject(testContext);
        iut.docIsValidAtomFeed();
    }
}
