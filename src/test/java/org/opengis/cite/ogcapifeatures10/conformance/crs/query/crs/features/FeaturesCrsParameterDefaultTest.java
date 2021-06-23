package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.features;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_CODE;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testng.ISuite;
import org.testng.ITestContext;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesCrsParameterDefaultTest {

    private static ITestContext testContext;

    private static ISuite suite;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );
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
        FeaturesCrsParameterDefault featuresDefaultCrsParameter = new FeaturesCrsParameterDefault();
        featuresDefaultCrsParameter.initCommonFixture( testContext );

        JsonPath collection = prepareCollection();
        featuresDefaultCrsParameter.verifyFeaturesCrsParameterDefault( "flurstueck", collection );
    }

    @Test(expected = AssertionError.class)
    public void test_unexpectedHeader() {
        prepareJadlerContentCrsHeaderUnexpected();
        FeaturesCrsParameterDefault featuresDefaultCrsParameter = new FeaturesCrsParameterDefault();
        featuresDefaultCrsParameter.initCommonFixture( testContext );

        JsonPath collection = prepareCollection();
        featuresDefaultCrsParameter.verifyFeaturesCrsParameterDefault( "flurstueck", collection );
    }

    private static JsonPath prepareCollection() {
        return new JsonPath( FeaturesCrsParameterDefaultTest.class.getResourceAsStream( "../../../../core/collections/collection-flurstueck.json" ) );
    }

    private void prepareJadler() {
        onRequest().havingPath( endsWith( "collections/flurstueck/items" ) ).havingQueryString( not( containsString( "crs=" ) ) ).respond().withHeader( "Content-Crs",
                                                                                                                                                        "<" + DEFAULT_CRS_CODE
                                                                                                                                                        + ">" ).withStatus( 200 );
    }

    private void prepareJadlerContentCrsHeaderUnexpected() {
        onRequest().havingPath( endsWith( "collections/flurstueck/items" ) ).havingQueryString( not( containsString( "crs=" ) ) ).respond().withHeader( "Content-Crs",
                                                                                                                                                        "<" + "www.opengis.net/def/crs/EPSG/0/25832"
                                                                                                                                                                       + ">" ).withStatus( 200 );
    }
}
