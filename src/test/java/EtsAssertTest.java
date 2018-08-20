import org.junit.Test;
import org.opengis.cite.wfs30.EtsAssert;

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

}
