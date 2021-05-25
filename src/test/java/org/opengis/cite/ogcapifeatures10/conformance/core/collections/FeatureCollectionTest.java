package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
public class FeatureCollectionTest {

    private static ITestContext testContext;

    private static ISuite suite;

    private static URI iut;

    @BeforeClass
    public static void instantiateUri()
                            throws URISyntaxException {
        iut = new URI( "http://localhost:8080/oaf" );
    }

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        OpenApi3Parser parser = new OpenApi3Parser();
        URL openAppiDocument = FeatureCollectionTest.class.getResource( "../../../openapi3/openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );

        List<RequirementClass> requirementClasses = new ArrayList();
        requirementClasses.add( RequirementClass.CORE );
        List<Map<String, Object>> collections = prepareCollections();

        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        URI landingPageUri = new URI( "https://www.ldproxy.nrw.de/kataster" );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
        when( suite.getAttribute( SuiteAttribute.API_MODEL.getName() ) ).thenReturn( apiModel );
        when( suite.getAttribute( SuiteAttribute.REQUIREMENTCLASSES.getName() ) ).thenReturn( requirementClasses );
        when( suite.getAttribute( SuiteAttribute.COLLECTIONS.getName() ) ).thenReturn( collections );
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
    public void testValidateFeatureCollectionMetadataOperationResponse() {
        prepareJadler();
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.initCommonFixture( testContext );
        featureCollection.retrieveApiModel( testContext );
        featureCollection.requirementClasses( testContext );

        Object[][] collections = featureCollection.collections( testContext );
        for ( Object[] object : collections ) {
            Map<String, Object> collection = (Map<String, Object>) object[0];
            featureCollection.validateFeatureCollectionMetadataOperation( testContext, collection );
            featureCollection.validateFeatureCollectionMetadataResponse( collection );
        }
    }

    private static List<Map<String, Object>> prepareCollections() {
        List<Map<String, Object>> collections = new ArrayList<>();
        collections.add( new JsonPath( FeatureCollectionTest.class.getResourceAsStream( "collection-flurstueck.json" ) ).get() );
        collections.add( new JsonPath( FeatureCollectionTest.class.getResourceAsStream( "collection-gebaeudebauwerk.json" ) ).get() );
        return collections;
    }

    private void prepareJadler() {
        InputStream collections = getClass().getResourceAsStream( "collections.json" );
        onRequest().havingPath( endsWith( "collections" ) ).respond().withBody( collections );

        InputStream collectionFlurstueck = getClass().getResourceAsStream( "collection-flurstueck.json" );
        onRequest().havingPath( endsWith( "collections/flurstueck" ) ).respond().withBody( collectionFlurstueck );

        InputStream collectionGebaeudebauwerk = getClass().getResourceAsStream( "collection-gebaeudebauwerk.json" );
        onRequest().havingPath( endsWith( "collections/gebaeudebauwerk" ) ).respond().withBody( collectionGebaeudebauwerk );

        InputStream collectionVerwaltungseinheit = getClass().getResourceAsStream( "collection-verwaltungseinheit.json" );
        onRequest().havingPath( endsWith( "collections/verwaltungseinheit" ) ).respond().withBody( collectionVerwaltungseinheit );
    }

}
