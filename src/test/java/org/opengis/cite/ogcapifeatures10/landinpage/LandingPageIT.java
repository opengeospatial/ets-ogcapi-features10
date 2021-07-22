package org.opengis.cite.ogcapifeatures10.landinpage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.conformance.core.landingpage.LandingPage;
import org.testng.ISuite;
import org.testng.ITestContext;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Ignore("Stable service is required")
public class LandingPageIT {

    private static ITestContext testContext;

    private static ISuite suite;

    private static final String SUT = "https://www.ldproxy.nrw.de/kataster";

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        URI landingPageUri = new URI( SUT );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
    }

    @Test
    public void testLandingPage() {
        LandingPage landingPage = new LandingPage();
        landingPage.initCommonFixture( testContext );
        landingPage.landingPageRetrieval();
        landingPage.landingPageValidation();
    }

}
