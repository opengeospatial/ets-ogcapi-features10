package org.opengis.cite.ogcapifeatures10.collections;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.conformance.RequirementClass;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.testng.ISuite;
import org.testng.ITestContext;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesTest {

    private static ITestContext testContext;

    private static ISuite suite;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        OpenApi3Parser parser = new OpenApi3Parser();
        URL openAppiDocument = FeaturesTest.class.getResource( "../openapi3/openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );

        InputStream json = FeaturesTest.class.getResourceAsStream( "../collections/collections.json" );
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

    @Before
    public void setUp() {
        initJadlerListeningOn( 8090 );
    }

    @After
    public void tearDown() {
        closeJadler();
    }

    @Test
    public void testGetFeatureOperations() {
        prepareJadler();
        Features getFeaturesOperation = new Features();
        getFeaturesOperation.initCommonFixture( testContext );
        getFeaturesOperation.retrieveRequiredInformationFromTestContext( testContext );
        getFeaturesOperation.requirementClasses( testContext );

        Iterator<Object[]> collections = getFeaturesOperation.collectionItemUris( testContext );
        for ( Iterator<Object[]> it = collections; it.hasNext(); ) {
            Object[] collection = it.next();
            Map<String, Object> parameter = (Map<String, Object>) collection[0];
            getFeaturesOperation.validateFeaturesOperation( testContext, parameter );
            getFeaturesOperation.validateFeaturesOperationResponse_Links( parameter );
            // skipped (parameter missing):
            // getFeaturesOperation.validateFeaturesOperationResponse_property_timeStamp( parameter );
            // skipped (parameter missing):
            // getFeaturesOperation.validateGetFeaturesOperationResponse_property_numberReturned( parameter );
            // skipped (parameter missing):
            // getFeaturesOperation.validateFeaturesOperationResponse_property_numberMatched( parameter );
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
            getFeaturesOperation.limitParameter_requests( parameter, 25 );
        }

        /*
         * Iterator<Object[]> collectionsWithBboxes = getFeaturesOperation.collectionItemUrisWithBboxes( testContext );
         * for ( Iterator<Object[]> collectionWithBbox = collectionsWithBboxes; collectionWithBbox.hasNext(); ) {
         * Object[] collection = collectionWithBbox.next(); Map<String, Object> parameter = (Map<String, Object>)
         * collection[0]; BBox bbox = (BBox) collection[1]; // fails: in collections.json must the links (rel: item,
         * type: application/geo+json) changed to https // getFeaturesOperation.boundingBoxParameter_requests(
         * parameter, bbox ); }
         */
        /*
         * Iterator<Object[]> collectionsWithTimes = getFeaturesOperation.collectionItemUrisWithTimes( testContext );
         * for ( Iterator<Object[]> it = collectionsWithTimes; it.hasNext(); ) { Object[] collection = it.next();
         * Map<String, Object> parameter = (Map<String, Object>) collection[0]; String queryParam = (String)
         * collection[1]; Object begin = collection[2]; Object end = collection[3];
         * getFeaturesOperation.timeParameter_requests( parameter, queryParam, begin, end ); }
         */
    }

    private void prepareJadler() {
        InputStream flurstueckItems = getClass().getResourceAsStream( "collectionItems-flurstueck.json" );
        onRequest().havingPath( endsWith( "collections/flurstueck/items" ) ).havingParameter( "limit", nullValue() ).respond().withBody( flurstueckItems );

        InputStream gebaeudebauwerkItems = getClass().getResourceAsStream( "collectionItems-gebaeudebauwerk.json" );
        onRequest().havingPath( endsWith( "collections/gebaeudebauwerk/items" ) ).respond().withBody( gebaeudebauwerkItems );

        InputStream flurstueckItemsLimit = getClass().getResourceAsStream( "collectionItems-flurstueck.json" );
        onRequest().havingPath( containsString( "collections/flurstueck/items" ) ).havingParameterEqualTo( "limit",
                                                                                                           "25" ).respond().withBody( flurstueckItemsLimit );

        InputStream verwaltungseinheitItems = getClass().getResourceAsStream( "collectionItems-verwaltungseinheit.json" );
        onRequest().havingPath( endsWith( "collections/verwaltungseinheit/items" ) ).respond().withBody( verwaltungseinheitItems );
    }

}
