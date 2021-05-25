package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
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
public class FeaturesLimitTest {

    private static ITestContext testContext;

    private static ISuite suite;

    private static TestPoint testPoint;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        OpenApi3Parser parser = new OpenApi3Parser();
        URL openAppiDocument = FeaturesLimitTest.class.getResource( "../../../openapi3/openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );

        InputStream json = FeaturesLimitTest.class.getResourceAsStream( "../collections/collections.json" );
        JsonPath collectionsResponse = new JsonPath( json );
        List<Map<String, Object>> collections = collectionsResponse.getList( "collections" );

        List<RequirementClass> requirementClasses = new ArrayList();
        requirementClasses.add( RequirementClass.CORE );

        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        testPoint = new TestPoint( "http://localhost:8090/rest/services/kataster", "/collections/flurstueck/items",
                                   Collections.emptyMap() );

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
    public void testParameterDefinition() {
        prepareJadler();
        FeaturesLimit features = initFeaturesLimit();

        features.limitParameterDefinition( testPoint );
    }

    @Test
    public void test() {
        prepareJadler();
        FeaturesLimit features = initFeaturesLimit();

        Map<String, Object> collection = prepareCollection();
        features.validateFeaturesWithLimitOperation( collection, 10, 15 );
        features.validateFeaturesWithLimitResponse_TypeProperty( collection, 10, 15 );
        features.validateFeaturesWithLimitResponse_FeaturesProperty( collection, 10, 15 );
        features.validateFeaturesWithLimitResponse_Links( collection, 10, 15 );
        // skipped (collection missing):
        // features.validateFeaturesWithLimitResponse_TimeStamp( collection, 10, 15 );
        // skipped (collection missing):
        // features.validateFeaturesWithLimitResponse_NumberMatched( collection , 10, 15 );
        // skipped (collection missing):
        // features.validateFeaturesResponse_NumberReturned( collection, 10, 15 );
    }

    private FeaturesLimit initFeaturesLimit() {
        FeaturesLimit features = new FeaturesLimit();
        features.initCommonFixture( testContext );
        features.retrieveRequiredInformationFromTestContext( testContext );
        features.requirementClasses( testContext );
        features.retrieveApiModel( testContext );
        return features;
    }

    private static Map<String, Object> prepareCollection() {
        return new JsonPath( FeatureCollectionTest.class.getResourceAsStream( "collection-flurstueck.json" ) ).get();
    }

    private void prepareJadler() {
        InputStream flurstueckItems = getClass().getResourceAsStream( "collectionItems-flurstueck.json" );
        onRequest().havingPath( endsWith( "collections/flurstueck/items" ) ).havingParameter( "limit",
                                                                                              nullValue() ).respond().withBody( flurstueckItems );

        InputStream flurstueckItemsLimit = getClass().getResourceAsStream( "collectionItems-flurstueck.json" );
        onRequest().havingPath( containsString( "collections/flurstueck/items" ) ).havingParameterEqualTo( "limit",
                                                                                                           "10" ).respond().withBody( flurstueckItemsLimit );
    }

}
