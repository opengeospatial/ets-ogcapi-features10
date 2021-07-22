package org.opengis.cite.ogcapifeatures10.conformance.crs.query.bboxcrs;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.UNSUPPORTED_CRS;

import java.net.URLEncoder;

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
public class BBoxCrsParameterInvalidTest {

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
    public void testVerifyBboxCrsParameterInvalid() {
        prepareJadler();
        BBoxCrsParameterInvalid bBoxCrsParameterInvalid = new BBoxCrsParameterInvalid();
        bBoxCrsParameterInvalid.initCommonFixture( testContext );

        JsonPath collection = prepareCollection();
        bBoxCrsParameterInvalid.verifyBboxCrsParameterInvalid( "vineyards", collection );
    }

    @Test(expected = AssertionError.class)
    public void testVerifyBboxCrsParameterInvalid_unexpectedResponse() {
        prepareJadlerResponseStatusCode200();
        BBoxCrsParameterInvalid bBoxCrsParameterInvalid = new BBoxCrsParameterInvalid();
        bBoxCrsParameterInvalid.initCommonFixture( testContext );

        JsonPath collection = prepareCollection();
        bBoxCrsParameterInvalid.verifyBboxCrsParameterInvalid( "vineyards", collection );
    }

    private static JsonPath prepareCollection() {
        return new JsonPath( BBoxCrsParameterInvalidTest.class.getResourceAsStream( "../collection-vineyards.json" ) );
    }

    private void prepareJadler() {
        onRequest().havingPath( endsWith( "collections/vineyards/items" ) ).havingQueryString( containsString( "bbox-crs="
                                                                                                               + URLEncoder.encode( UNSUPPORTED_CRS ) ) ).respond().withStatus( 400 );
    }

    private void prepareJadlerResponseStatusCode200() {
        onRequest().havingPath( endsWith( "collections/vineyards/items" ) ).havingQueryString( containsString( "bbox-crs="
                                                                                                               + URLEncoder.encode( UNSUPPORTED_CRS ) ) ).respond().withStatus( 200 );
    }
}
