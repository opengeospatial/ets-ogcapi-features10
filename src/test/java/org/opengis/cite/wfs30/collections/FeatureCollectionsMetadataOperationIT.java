package org.opengis.cite.wfs30.collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.opengis.cite.wfs30.openapi3.TestPoint;
import org.testng.ISuite;
import org.testng.ITestContext;

import com.reprezen.kaizen.oasparser.model3.MediaType;
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
        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        URI landingPageUri = new URI( "https://www.ldproxy.nrw.de/kataster" );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
    }

    @Test
    public void testValidateFeatureCollectionsMetadataOperationResponse() {
        FeatureCollectionsMetadataOperation featureCollectionsMetadataOperation = new FeatureCollectionsMetadataOperation();
        featureCollectionsMetadataOperation.initCommonFixture( testContext );
        UriTemplate conformanceUri = new UriTemplate( "https://www.ldproxy.nrw.de/kataster/collections" );
        TestPoint testPoint = new TestPoint( conformanceUri, mediaTypes() );
        featureCollectionsMetadataOperation.validateFeatureCollectionsMetadataOperationResponse( testPoint );
    }

    private Map<String, MediaType> mediaTypes() {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put( "application/json", Mockito.mock( MediaType.class ) );
        mediaTypes.put( "text/html", Mockito.mock( MediaType.class ) );
        return mediaTypes;
    }

}
