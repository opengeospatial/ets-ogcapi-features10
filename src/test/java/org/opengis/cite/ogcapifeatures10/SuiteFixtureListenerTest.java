package org.opengis.cite.ogcapifeatures10;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.listener.SuiteFixtureListener;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

public class SuiteFixtureListenerTest {

    private static XmlSuite xmlSuite;

    private static ISuite suite;

    @BeforeClass
    public static void setUpClass() {
        xmlSuite = mock( XmlSuite.class );
        suite = mock( ISuite.class );
        when( suite.getXmlSuite() ).thenReturn( xmlSuite );
    }

    @Test(expected = IllegalArgumentException.class)
    public void noSuiteParameters() {
        Map<String, String> params = new HashMap<>();
        when( xmlSuite.getParameters() ).thenReturn( params );
        SuiteFixtureListener iut = new SuiteFixtureListener();
        iut.onStart( suite );
    }

    @Test
    public void processIUTParameter()
                            throws URISyntaxException {
        URL url = this.getClass().getResource( "landingPage.html" );
        Map<String, String> params = new HashMap<>();
        params.put( TestRunArg.IUT.toString(), url.toURI().toString() );
        when( xmlSuite.getParameters() ).thenReturn( params );
        SuiteFixtureListener iut = new SuiteFixtureListener();
        iut.onStart( suite );
        verify( suite ).setAttribute( eq( SuiteAttribute.TEST_SUBJ_FILE.getName() ), isA( File.class ) );
    }

}
