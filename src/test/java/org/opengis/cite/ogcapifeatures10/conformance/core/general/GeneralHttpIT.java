package org.opengis.cite.ogcapifeatures10.conformance.core.general;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeneralHttpIT {

    private static ITestContext testContext;

    private static ISuite suite;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        URI landingPageUri = new URI( "https://www.ldproxy.nrw.de/kataster" );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
    }

    @Test
    public void testGeneralHttp()
                            throws Exception {
        GeneralHttp generalHttp = new GeneralHttp();
        generalHttp.initCommonFixture( testContext );
        generalHttp.http();
    }

}
