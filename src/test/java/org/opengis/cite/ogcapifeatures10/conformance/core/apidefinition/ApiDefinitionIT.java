package org.opengis.cite.ogcapifeatures10.conformance.core.apidefinition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Ignore("Stable service is required")
public class ApiDefinitionIT {

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
    public void testOpenApi()
                            throws Exception {
        ApiDefinition openApi = new ApiDefinition();
        openApi.initCommonFixture( testContext );
        openApi.retrieveApiUrl();
        openApi.openapiDocumentRetrieval();
        openApi.apiDefinitionValidation( testContext );
    }

}
