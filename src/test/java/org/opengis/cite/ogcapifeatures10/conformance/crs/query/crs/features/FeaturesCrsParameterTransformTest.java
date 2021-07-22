package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.features;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;

import java.io.InputStream;
import java.net.URLEncoder;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.io.ParseException;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.testng.ISuite;
import org.testng.ITestContext;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesCrsParameterTransformTest {

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
    public void test()
                            throws ParseException {
        prepareJadler();
        FeaturesCrsParameterTransform featuresCrsParameterTransform = new FeaturesCrsParameterTransform();
        featuresCrsParameterTransform.initCommonFixture( testContext );

        JsonPath collection = prepareCollection();
        featuresCrsParameterTransform.verifyFeaturesCrsParameterTransformWithoutCrsParameter( "vineyards", collection );
        featuresCrsParameterTransform.verifyFeaturesCrsParameterTransformWithCrsParameter( "vineyards", collection,
                                                                                           EPSG_25832, DEFAULT_CRS );
    }

    private static JsonPath prepareCollection() {
        return new JsonPath( FeaturesCrsParameterTransformTest.class.getResourceAsStream( "../../collection-vineyards.json" ) );
    }

    private void prepareJadler() {
        InputStream items = getClass().getResourceAsStream( "../../collectionItems-vineyards.json" );
        onRequest().havingPath( endsWith( "collections/vineyards/items" ) ).havingQueryString( not( containsString( "crs=" ) ) ).respond().withBody( items ).withHeader( "Content-Crs",
                                                                                                                                                                         DEFAULT_CRS.getAsHeaderValue() ).withStatus( 200 );
        InputStream itemsIn25832 = getClass().getResourceAsStream( "../../collectionItems-vineyards-25832.json" );
        onRequest().havingPath( endsWith( "collections/vineyards/items" ) ).havingQueryString( containsString( "crs="
                                                                                                               + URLEncoder.encode( EPSG_25832.getCode() ) ) ).respond().withBody( itemsIn25832 ).withHeader( "Content-Crs",
                                                                                                                                                                                                              EPSG_25832.getAsHeaderValue() ).withStatus( 200 );
    }

}
