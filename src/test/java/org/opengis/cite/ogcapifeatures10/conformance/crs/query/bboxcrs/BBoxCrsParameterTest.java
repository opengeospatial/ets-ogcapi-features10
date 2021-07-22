package org.opengis.cite.ogcapifeatures10.conformance.crs.query.bboxcrs;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;

import java.net.URLEncoder;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.testng.ISuite;
import org.testng.ITestContext;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBoxCrsParameterTest {

    private static final CoordinateSystem EPSG_25832 = new CoordinateSystem( "http://www.opengis.net/def/crs/EPSG/0/25832" );

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
    public void testVerifyBboxCrsParameter() {
        prepareJadler();
        BBoxCrsParameter bBoxCrsParameter = new BBoxCrsParameter();
        bBoxCrsParameter.initCommonFixture( testContext );

        JsonPath collection = prepareCollection();
        bBoxCrsParameter.verifyBboxCrsParameterWithDefaultCrs( "vineyards", collection, DEFAULT_CRS );
        bBoxCrsParameter.verifyBboxCrsParameter( "vineyards", collection, EPSG_25832, DEFAULT_CRS );
    }

    @Test(expected = AssertionError.class)
    public void testVerifyBboxCrsParameter_unexpectedResponse() {
        prepareJadlerUnexpectedResponse();
        BBoxCrsParameter bBoxCrsParameter = new BBoxCrsParameter();
        bBoxCrsParameter.initCommonFixture( testContext );

        JsonPath collection = prepareCollection();
        bBoxCrsParameter.verifyBboxCrsParameterWithDefaultCrs( "vineyards", collection, DEFAULT_CRS );
        bBoxCrsParameter.verifyBboxCrsParameter( "vineyards", collection, EPSG_25832, DEFAULT_CRS );
    }

    private static JsonPath prepareCollection() {
        return new JsonPath( BBoxCrsParameterTest.class.getResourceAsStream( "../collection-vineyards.json" ) );
    }

    private void prepareJadler() {
        onRequest().havingPath( endsWith( "collections/vineyards/items" ) ).havingQueryString( not( containsString( "bbox-crs=" ) ) ).respond().withBody( BBoxCrsParameterTest.class.getResourceAsStream( "../collectionItems-vineyards.json" ) ).withStatus( 200 );
        onRequest().havingPath( endsWith( "collections/vineyards/items" ) ).havingQueryString( containsString( "bbox-crs="
                                                                                                               + URLEncoder.encode( EPSG_25832.getCode() ) ) ).respond().withBody( BBoxCrsParameterTest.class.getResourceAsStream( "../collectionItems-vineyards-25832.json" ) ).withStatus( 200 );
    }

    private void prepareJadlerUnexpectedResponse() {
        onRequest().havingPath( endsWith( "collections/vineyards/items" ) ).havingQueryString( not( containsString( "bbox-crs=" ) ) ).respond().withBody( BBoxCrsParameterTest.class.getResourceAsStream( "../collectionItems-vineyards-offset-9.json" ) ).withStatus( 200 );
        onRequest().havingPath( endsWith( "collections/vineyards/items" ) ).havingQueryString( containsString( "bbox-crs="
                                                                                                               + URLEncoder.encode( EPSG_25832.getCode() ) ) ).respond().withBody( BBoxCrsParameterTest.class.getResourceAsStream( "../collectionItems-vineyards-25832.json" ) ).withStatus( 200 );
    }
}
