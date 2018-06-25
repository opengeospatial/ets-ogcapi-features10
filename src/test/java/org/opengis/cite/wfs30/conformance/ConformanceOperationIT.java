package org.opengis.cite.wfs30.conformance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.opengis.cite.wfs30.openapi3.TestPoint;
import org.testng.ISuite;
import org.testng.ITestContext;

import com.sun.jersey.api.uri.UriTemplate;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ConformanceOperationIT {

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
    public void testApiDefinition() {
        ConformanceOperation conformanceOperation = new ConformanceOperation();
        conformanceOperation.initCommonFixture( testContext );
        UriTemplate conformanceUri = new UriTemplate( "https://www.ldproxy.nrw.de/kataster/conformance" );
        TestPoint testPoint = new TestPoint( conformanceUri );
        conformanceOperation.validateConformanceOperationAndResponse( testPoint );
    }

}
