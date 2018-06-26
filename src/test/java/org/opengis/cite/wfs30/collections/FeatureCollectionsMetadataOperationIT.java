package org.opengis.cite.wfs30.collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.opengis.cite.wfs30.openapi3.TestPoint;
import org.testng.ISuite;
import org.testng.ITestContext;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.sun.jersey.api.uri.UriTemplate;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureCollectionsMetadataOperationIT {

    private static ITestContext testContext;

    private static ISuite suite;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        OpenApi3Parser parser = new OpenApi3Parser();
        URL openAppiDocument = FeatureCollectionsMetadataOperationIT.class.getResource( "../openapi3/openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );

        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        URI landingPageUri = new URI( "https://www.ldproxy.nrw.de/kataster" );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
        when( suite.getAttribute( SuiteAttribute.API_MODEL.getName() ) ).thenReturn( apiModel );
    }

    @Test
    public void testValidateFeatureCollectionsMetadataOperationResponse() {
        FeatureCollectionsMetadataOperation featureCollectionsMetadataOperation = new FeatureCollectionsMetadataOperation();
        featureCollectionsMetadataOperation.initCommonFixture( testContext );
        featureCollectionsMetadataOperation.parseRequiredMetadata( testContext );
        featureCollectionsMetadataOperation.openApiDocument( testContext );
        UriTemplate conformanceUri = new UriTemplate( "https://www.ldproxy.nrw.de/kataster/collections" );
        TestPoint testPoint = new TestPoint( conformanceUri, mediaTypes() );
        featureCollectionsMetadataOperation.validateFeatureCollectionsMetadataOperation( testPoint );
        featureCollectionsMetadataOperation.validateFeatureCollectionsMetadataOperationResponse_Links( testPoint );
        featureCollectionsMetadataOperation.validateFeatureCollectionsMetadataOperationResponse_Collections( testPoint );

        Object[][] collections = featureCollectionsMetadataOperation.collections( testContext );
        for ( Object[] object : collections ) {
            TestPoint tp = (TestPoint) object[0];
            Map<String, Object> collection = (Map<String, Object>) object[1];
            featureCollectionsMetadataOperation.validateCollectionsMetadataResponse_Links( tp, collection );
            featureCollectionsMetadataOperation.validateCollectionsMetadataResponse_Extent( tp, collection );
            featureCollectionsMetadataOperation.validateFeatureCollectionMetadataOperation( tp, collection );
        }
    }

    private Map<String, MediaType> mediaTypes() {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put( "application/json", Mockito.mock( MediaType.class ) );
        mediaTypes.put( "text/html", Mockito.mock( MediaType.class ) );
        return mediaTypes;
    }

}
