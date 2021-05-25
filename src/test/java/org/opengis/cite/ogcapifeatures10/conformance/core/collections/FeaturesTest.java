package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.conformance.RequirementClass;
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
        URL openAppiDocument = FeaturesTest.class.getResource( "../../../openapi3/openapi.json" );
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
    public void test() {
        prepareJadler();
        Features features = new Features();
        features.initCommonFixture( testContext );
        features.retrieveRequiredInformationFromTestContext( testContext );
        features.requirementClasses( testContext );

        Map<String, Object> collection = prepareCollection();
        features.validateFeaturesOperation( testContext, collection );
        features.validateFeaturesResponse_TypeProperty( collection );
        features.validateFeaturesResponse_FeaturesProperty( collection );
        features.validateFeaturesResponse_Links( collection );
        // skipped (collection missing):
        // features.validateFeaturesResponse_TimeStamp( collection );
        // skipped (collection missing):
        // features.validateFeaturesResponse_NumberMatched( collection );
        // skipped (collection missing):
        // features.validateFeaturesResponse_NumberReturned( collection );
    }

    private static Map<String, Object> prepareCollection() {
        return new JsonPath( FeatureCollectionTest.class.getResourceAsStream( "collection-flurstueck.json" ) ).get();
    }

    private void prepareJadler() {
        InputStream flurstueckItems = getClass().getResourceAsStream( "collectionItems-flurstueck.json" );
        onRequest().havingPath( endsWith( "collections/flurstueck/items" ) ).respond().withBody( flurstueckItems );
    }

}
