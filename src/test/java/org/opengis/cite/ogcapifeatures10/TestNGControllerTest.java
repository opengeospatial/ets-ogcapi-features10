package org.opengis.cite.ogcapifeatures10;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Verifies the results of executing a test run using the main controller (TestNGController).
 */
public class TestNGControllerTest {

    private static DocumentBuilder docBuilder;

    private Properties testRunProps;

    @BeforeClass
    public static void initParser()
                            throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        dbf.setValidating( false );
        dbf.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
        docBuilder = dbf.newDocumentBuilder();
    }

    @Before
    public void loadDefaultTestRunProperties()
                            throws IOException {
        this.testRunProps = new Properties();
        this.testRunProps.loadFromXML( getClass().getResourceAsStream( "/test-run-props.xml" ) );
    }

    @Test
    public void testValidateTestRunArgs()
                            throws Exception {
        URL testSubject = getClass().getResource( "landingPage.html" );
        this.testRunProps.setProperty( TestRunArg.IUT.toString(), testSubject.toURI().toString() );
        ByteArrayOutputStream outStream = new ByteArrayOutputStream( 1024 );
        this.testRunProps.storeToXML( outStream, "Integration test" );
        Document testRunArgs = docBuilder.parse( new ByteArrayInputStream( outStream.toByteArray() ) );

        TestNGController controller = new TestNGController();
        controller.validateTestRunArgs( testRunArgs );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateTestRunArgs_missingIUT()
                            throws Exception {
        this.testRunProps.remove( TestRunArg.IUT.toString() );
        ByteArrayOutputStream outStream = new ByteArrayOutputStream( 1024 );
        this.testRunProps.storeToXML( outStream, "Integration test" );
        Document testRunArgs = docBuilder.parse( new ByteArrayInputStream( outStream.toByteArray() ) );

        TestNGController controller = new TestNGController();
        controller.validateTestRunArgs( testRunArgs );
    }

}
