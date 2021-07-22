package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.feature;

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
public class FeatureCrsParameterTest {

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
        FeatureCrsParameter featureValidCrsParameter = new FeatureCrsParameter();
        featureValidCrsParameter.initCommonFixture( testContext );

        JsonPath collection = prepareCollection();
        featureValidCrsParameter.verifyFeatureCrsParameter( "flurstueck", collection, "testId", DEFAULT_CRS );
    }

    private static JsonPath prepareCollection() {
        return new JsonPath( FeatureCrsParameterTest.class.getResourceAsStream( "../../../../core/collections/collection-flurstueck.json" ) );
    }

    private void prepareJadler() {
        InputStream flurstueckItem = getClass().getResourceAsStream( "../../../../core/collections/collectionItem1-flurstueck.json" );
        String expectedHeader = DEFAULT_CRS.getAsHeaderValue();
        onRequest().havingPath( endsWith( "collections/flurstueck/items/testId" ) ).respond().withBody( flurstueckItem ).withHeader( "Content-Crs",
                                                                                                                                     expectedHeader );
    }

}
