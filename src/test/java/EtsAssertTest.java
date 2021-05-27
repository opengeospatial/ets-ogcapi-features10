import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertValidCrsIdentifier;

import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.EtsAssert;

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
        assertValidCrsIdentifier( "http://www.opengis.net/def/crs/OGC/1.3/CRS84", "OK" );
    }

    @Test
    public void testAssertValidCrsIdentifier_OGC_URN() {
        assertValidCrsIdentifier( "urn:ogc:def:crs:OGC:1.3:CRS84", "OK" );
    }

    @Test
    public void testAssertValidCrsIdentifier_URL() {
        assertValidCrsIdentifier( "http://www.test.de/crs/4326", "OK" );
    }

    @Test
    public void testAssertValidCrsIdentifier_URN() {
        assertValidCrsIdentifier( "urn:test:crs:CRS84", "OK" );
    }

    @Test(expected = AssertionError.class)
    public void testAssertValidCrsIdentifier_null() {
        assertValidCrsIdentifier( null, "FAIlURE" );
    }

    @Test(expected = AssertionError.class)
    public void testAssertValidCrsIdentifier_empty() {
        assertValidCrsIdentifier( "", "FAIlURE" );
    }

}
