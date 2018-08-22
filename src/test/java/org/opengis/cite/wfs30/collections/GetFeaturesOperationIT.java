package org.opengis.cite.wfs30.collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.opengis.cite.wfs30.conformance.RequirementClass;
import org.opengis.cite.wfs30.openapi3.TestPoint;
import org.opengis.cite.wfs30.util.BBox;
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
        URL openAppiDocument = GetFeaturesOperationIT.class.getResource( "../openapi3/openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );

        InputStream json = GetFeaturesOperationIT.class.getResourceAsStream( "../collections/collections.json" );
        JsonPath collectionsResponse = new JsonPath( json );
        List<Map<String, Object>> collections = collectionsResponse.getList( "collections" );

        List<RequirementClass> requirementClasses = new ArrayList();
        requirementClasses.add( RequirementClass.CORE );

        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        URI landingPageUri = new URI( "https://www.ldproxy.nrw.de/kataster" );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
        when( suite.getAttribute( SuiteAttribute.API_MODEL.getName() ) ).thenReturn( apiModel );
        when( suite.getAttribute( SuiteAttribute.COLLECTIONS.getName() ) ).thenReturn( collections );
        when( suite.getAttribute( SuiteAttribute.REQUIREMENTCLASSES.getName() ) ).thenReturn( requirementClasses );
    }

    @Test
    public void testGetFeatureOperations()
                            throws URISyntaxException {
        GetFeaturesOperation getFeaturesOperation = new GetFeaturesOperation();
        getFeaturesOperation.initCommonFixture( testContext );
        getFeaturesOperation.retrieveRequiredInformationFromTestContext( testContext );
        getFeaturesOperation.requirementClasses( testContext );

        Iterator<Object[]> collections = getFeaturesOperation.collectionItemUris( testContext );
        for ( Iterator<Object[]> it = collections; it.hasNext(); ) {
            Object[] collection = it.next();
            Map<String, Object> parameter = (Map<String, Object>) collection[0];
            getFeaturesOperation.validateTheGetFeaturesOperation( testContext, parameter );
            getFeaturesOperation.validateTheGetFeaturesOperationResponse_Links( parameter );
            // skipped (parameter missing):
            // getFeaturesOperation.validateTheGetFeaturesOperationResponse_property_timeStamp( parameter );
            // skipped (parameter missing):
            // getFeaturesOperation.validateGetFeaturesOperationResponse_property_numberReturned( parameter );
            // skipped (parameter missing):
            // getFeaturesOperation.validateTheGetFeaturesOperationResponse_property_numberMatched( parameter );
        }

        Iterator<Object[]> collectionPaths = getFeaturesOperation.collectionPaths( testContext );
        for ( Iterator<Object[]> it = collectionPaths; it.hasNext(); ) {
            Object[] collectionPath = it.next();
            TestPoint testPoint = (TestPoint) collectionPath[0];
            getFeaturesOperation.limitParameter( testPoint );
            // fails (parameter is missing):
            // getFeaturesOperation.timeParameter( testPoint );
            // fails (schema->items->type missing):
            // getFeaturesOperation.boundingBoxParameter( testPoint );
        }

        Iterator<Object[]> collectionsWithLimits = getFeaturesOperation.collectionItemUrisWithLimits( testContext );
        for ( Iterator<Object[]> it = collectionsWithLimits; it.hasNext(); ) {
            Object[] collection = it.next();
            Map<String, Object> parameter = (Map<String, Object>) collection[0];
            int limit = (int) collection[1];
            // skipped (parameter missing):
            getFeaturesOperation.limitParameter_requests( parameter, limit );
        }

        Iterator<Object[]> collectionsWithBboxes = getFeaturesOperation.collectionItemUrisWithBboxes( testContext );
        for ( Iterator<Object[]> collectionWithBbox = collectionsWithBboxes; collectionWithBbox.hasNext(); ) {
            Object[] collection = collectionWithBbox.next();
            Map<String, Object> parameter = (Map<String, Object>) collection[0];
            BBox bbox = (BBox) collection[1];
            // fails: in collections.json must the links (rel: item, type: application/geo+json) changed to https
            // getFeaturesOperation.boundingBoxParameter_requests( parameter, bbox );
        }

        Iterator<Object[]> collectionsWithTimes = getFeaturesOperation.collectionItemUrisWithTimes( testContext );
        for ( Iterator<Object[]> it = collectionsWithTimes; it.hasNext(); ) {
            Object[] collection = it.next();
            Map<String, Object> parameter = (Map<String, Object>) collection[0];
            String queryParam = (String) collection[1];
            Object begin = collection[2];
            Object end = collection[3];
            getFeaturesOperation.timeParameter_requests( parameter, queryParam, begin, end );
        }
    }

}
