package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.features;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;

import java.io.InputStream;

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
public class FeaturesCrsParameterTest {

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
        FeaturesCrsParameter featuresValidCrsParameter = new FeaturesCrsParameter();
        featuresValidCrsParameter.initCommonFixture( testContext );

        JsonPath collection = prepareCollection();
        featuresValidCrsParameter.verifyFeaturesCrsParameter( "flurstueck", collection, DEFAULT_CRS );
    }

    private static JsonPath prepareCollection() {
        return new JsonPath( FeaturesCrsParameterTest.class.getResourceAsStream( "../../../../core/collections/collection-flurstueck.json" ) );
    }

    private void prepareJadler() {
        InputStream flurstueckItems = getClass().getResourceAsStream( "../../../../core/collections/collectionItems-flurstueck.json" );
        String expectedHeader = DEFAULT_CRS.getAsHeaderValue();
        onRequest().havingPath( endsWith( "collections/flurstueck/items" ) ).respond().withBody( flurstueckItems ).withHeader( "Content-Crs",
                                                                                                                               expectedHeader );
    }

}
