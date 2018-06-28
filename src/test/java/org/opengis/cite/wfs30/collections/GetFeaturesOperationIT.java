package org.opengis.cite.wfs30.collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetFeaturesOperationIT {

    private static ITestContext testContext;

    private static ISuite suite;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        OpenApi3Parser parser = new OpenApi3Parser();
        URL openAppiDocument = FeatureCollectionsMetadataOperationIT.class.getResource( "../openapi3/openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );

        InputStream json = GetFeaturesOperationIT.class.getResourceAsStream( "../collections/collections.json" );
        JsonPath collectionsResponse = new JsonPath( json );
        List<Map<String, Object>> collections = collectionsResponse.getList( "collections" );

        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        URI landingPageUri = new URI( "https://www.ldproxy.nrw.de/kataster" );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
        when( suite.getAttribute( SuiteAttribute.API_MODEL.getName() ) ).thenReturn( apiModel );
        when( suite.getAttribute( SuiteAttribute.COLLECTIONS.getName() ) ).thenReturn( collections );
    }

    @Test
    public void testGetFeatureOperations()
                            throws URISyntaxException {
        GetFeaturesOperation getFeaturesOperation = new GetFeaturesOperation();
        getFeaturesOperation.initCommonFixture( testContext );
        getFeaturesOperation.retrieveRequiredInformationFromTestContext( testContext );

        Object[][] collections = getFeaturesOperation.collectionItemUris( testContext );
        for ( Object[] collection : collections ) {
            Map<String, Object> parameter = (Map<String, Object>) collection[0];
            getFeaturesOperation.validateGetFeaturesOperation( parameter );
            getFeaturesOperation.validateGetFeaturesOperationResponse_Links( parameter );
            // skipped (parameter missing):
            // getFeaturesOperation.validateGetFeaturesOperationResponse_property_timeStamp( parameter );
            // skipped (parameter missing):
            // getFeaturesOperation.validateGetFeaturesOperationResponse_property_numberReturned( parameter );
            // skipped (parameter missing):
            // getFeaturesOperation.validateGetFeaturesOperationResponse_property_numberMatched( parameter );
        }
        getFeaturesOperation.validateLimitParameter();
        // fails (schema->items->type missing): getFeaturesOperation.validateBboxParameter();
        getFeaturesOperation.validateTimeParameter();
    }

}
