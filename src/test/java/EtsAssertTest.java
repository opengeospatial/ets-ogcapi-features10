import static org.hamcrest.CoreMatchers.is;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertCrsHeader;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertDefaultCrs;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertValidCrsIdentifier;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.EtsAssert;
import org.opengis.cite.ogcapifeatures10.OgcApiFeatures10;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class EtsAssertTest {

    @Test
    public void testAssertTrue() {
        EtsAssert.assertTrue( true, "OK" );
    }

    @Test
    public void testAssertFalse() {
        EtsAssert.assertFalse( false, "OK" );
    }

    @Test(expected = AssertionError.class)
    public void testAssertTrue_false() {
        EtsAssert.assertTrue( false, "FAIlURE" );
    }

    @Test(expected = AssertionError.class)
    public void testAssertFalse_true() {
        EtsAssert.assertFalse( true, "FAIlURE" );
    }

    @Test
    public void testAssertValidCrsIdentifier_OGC_URL() {
        assertValidCrsIdentifier( new CoordinateSystem( "http://www.opengis.net/def/crs/OGC/1.3/CRS84" ), "OK" );
    }

    @Test
    public void testAssertValidCrsIdentifier_OGC_URN() {
        assertValidCrsIdentifier( new CoordinateSystem( "urn:ogc:def:crs:OGC:1.3:CRS84" ), "OK" );
    }

    @Test
    public void testAssertValidCrsIdentifier_URL() {
        assertValidCrsIdentifier( new CoordinateSystem( "http://www.test.de/crs/4326" ), "OK" );
    }

    @Test
    public void testAssertValidCrsIdentifier_URN() {
        assertValidCrsIdentifier( new CoordinateSystem( "urn:test:crs:CRS84" ), "OK" );
    }

    @Test(expected = AssertionError.class)
    public void testAssertValidCrsIdentifier_null() {
        assertValidCrsIdentifier( null, "FAIlURE" );
    }

    @Test(expected = AssertionError.class)
    public void testAssertValidCrsIdentifier_empty() {
        assertValidCrsIdentifier( new CoordinateSystem( "" ), "FAIlURE" );
    }

    @Test
    public void testAssertDefaultCrs() {
        CoordinateSystem defaultCrs = assertDefaultCrs( Arrays.asList( "urn:test:crs:CRS84",
                                                                       OgcApiFeatures10.DEFAULT_CRS_CODE ),
                                                        "OK" );
        Assert.assertThat( defaultCrs, is( OgcApiFeatures10.DEFAULT_CRS ) );
    }

    @Test(expected = AssertionError.class)
    public void testAssertDefaultCrs_Missing() {
        assertDefaultCrs( Arrays.asList( "urn:test:crs:CRS84" ), "OK" );
    }

    @Test
    public void testAssertCrsHeader() {
        assertCrsHeader( "<http://www.opengis.net/def/crs/OGC/1.3/CRS84>",
                         new CoordinateSystem( "http://www.opengis.net/def/crs/OGC/1.3/CRS84" ), "OK" );
    }

    @Test(expected = AssertionError.class)
    public void testAssertCrsHeader_UnexpectedCode() {
        assertCrsHeader( "<http://www.opengis.net/def/crs/OGC/0/25832>",
                         new CoordinateSystem( "http://www.opengis.net/def/crs/OGC/1.3/CRS84" ), "FAIlURE" );
    }

    @Test(expected = AssertionError.class)
    public void testAssertCrsHeader_MissingBracket() {
        assertCrsHeader( "http://www.opengis.net/def/crs/OGC/1.3/CRS84",
                         new CoordinateSystem( "http://www.opengis.net/def/crs/OGC/1.3/CRS84" ), "FAIlURE" );
    }
}
