package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opengis.cite.ogcapifeatures10.conformance.core.collections.FeaturesErrorConditions.INVALID_QUERY_PARAM_VALUE;
import static org.opengis.cite.ogcapifeatures10.conformance.core.collections.FeaturesErrorConditions.UNKNOWN_QUERY_PARAM;

import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesErrorConditionsTest {

    private static ITestContext testContext;

    private static ISuite suite;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        OpenApi3Parser parser = new OpenApi3Parser();
        URL openAppiDocument = FeaturesTest.class.getResource( "../../../openapi3/openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );

        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        URI landingPageUri = new URI( "https://www.ldproxy.nrw.de/kataster" );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
        when( suite.getAttribute( SuiteAttribute.API_MODEL.getName() ) ).thenReturn( apiModel );
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
    public void testValidateFeaturesOperation_QueryParamInvalid() {
        prepareJadler();
        FeaturesErrorConditions featuresErrorConditions = new FeaturesErrorConditions();
        featuresErrorConditions.initCommonFixture( testContext );
        featuresErrorConditions.retrieveRequiredInformationFromTestContext( testContext );
        featuresErrorConditions.requirementClasses( testContext );

        Map<String, Object> parameter = prepareCollection();
        featuresErrorConditions.validateFeaturesOperation_QueryParamInvalid( parameter );
    }

    @Ignore
    @Test
    public void testValidateFeaturesOperation_QueryParamUnknown() {
        prepareJadler();
        FeaturesErrorConditions featuresErrorConditions = new FeaturesErrorConditions();
        featuresErrorConditions.initCommonFixture( testContext );
        featuresErrorConditions.retrieveRequiredInformationFromTestContext( testContext );
        featuresErrorConditions.requirementClasses( testContext );

        Map<String, Object> parameter = prepareCollection();
        featuresErrorConditions.validateFeaturesOperation_QueryParamUnknown( parameter );
    }

    private static Map<String, Object> prepareCollection() {
        return new JsonPath( FeatureCollectionTest.class.getResourceAsStream( "collection-flurstueck.json" ) ).get();
    }

    private void prepareJadler() {
        onRequest().havingPath( endsWith( "collections/flurstueck/items" ) ).havingParameterEqualTo( "limit",
                                                                                                     INVALID_QUERY_PARAM_VALUE ).respond().withStatus( 400 );
        onRequest().havingPath( endsWith( "collections/flurstueck/items" ) ).havingParameter( UNKNOWN_QUERY_PARAM ).respond().withStatus( 400 );
    }
}