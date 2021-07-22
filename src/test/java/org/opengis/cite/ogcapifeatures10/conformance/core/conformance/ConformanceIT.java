package org.opengis.cite.ogcapifeatures10.conformance.core.conformance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.testng.ISuite;
import org.testng.ITestContext;

import com.reprezen.kaizen.oasparser.model3.MediaType;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Ignore("Stable service is required")
public class ConformanceIT {

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
        Conformance conformanceOperation = new Conformance();
        conformanceOperation.initCommonFixture( testContext );
        TestPoint testPoint = new TestPoint( "https://www.ldproxy.nrw.de/kataster", "/conformance", mediaTypes() );
        conformanceOperation.validateConformanceOperationAndResponse( testPoint );
    }

    private Map<String, MediaType> mediaTypes() {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put( "application/json", Mockito.mock( MediaType.class ) );
        mediaTypes.put( "text/html", Mockito.mock( MediaType.class ) );
        return mediaTypes;
    }

}
