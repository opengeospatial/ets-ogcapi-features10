package org.opengis.cite.ogcapifeatures10;

import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;
import org.opengis.cite.ogcapifeatures10.util.OgcNameValidator;
import org.opengis.cite.ogcapifeatures10.util.UrnValidator;

/**
 * Provides a set of custom assertion methods.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class EtsAssert {

    /**
     * @param valueToAssert
     *            the boolean to assert to be <code>true</code>
     * @param failureMsg
     *            the message to throw in case of a failure, should not be <code>null</code>
     */
    public static void assertTrue( boolean valueToAssert, String failureMsg ) {
        if ( !valueToAssert )
            throw new AssertionError( failureMsg );
    }

    /**
     * @param valueToAssert
     *            the boolean to assert to be <code>false</code>
     * @param failureMsg
     *            the message to throw in case of a failure, should not be <code>null</code>
     */
    public static void assertFalse( boolean valueToAssert, String failureMsg ) {
        if ( valueToAssert )
            throw new AssertionError( failureMsg );
    }

    /**
     * <pre>
     *  1. For http-URIs (starting with http:) validate that the string conforms to the syntax specified  
     *     by RFC 7230, section 2.7.1. 
     *  2. For https-URIs (starting with https:) validate that the string conforms to the syntax specified 
     *     by RFC 7230, section 2.7.2. 
     *  3. For URNs (starting with urn:) validate that the string conforms to the syntax specified 
     *     by RFC 8141, section 2. 
     *  4. For OGC URNs (starting with urn:ogc:def:crs:) and OGC http-URIs (starting with http://www.opengis.net/def/crs/) 
     *     validate that the string conforms to the syntax specified by OGC Name Type Specification - definitions - part 1 – basic name.
     * </pre>
     * 
     * @param valueToAssert
     *            the value to assert as valid identifier (see above). If <code>null</code> an AssertionError is thrown.
     * @param failureMsg
     *            the message to throw in case of a failure, should not be <code>null</code>
     */
    public static void assertValidCrsIdentifier( String valueToAssert, String failureMsg ) {
        if ( valueToAssert == null ) {
            throw new AssertionError( failureMsg );
        }
        if ( valueToAssert.startsWith( "http:" ) && !valueToAssert.startsWith( "http://www.opengis.net/def/crs/" ) ) {
            assertValidHttpCrsIdentifier( valueToAssert, failureMsg );
        } else if ( valueToAssert.startsWith( "https:" ) ) {
            assertValidHttpCrsIdentifier( valueToAssert, failureMsg );
        } else if ( valueToAssert.startsWith( "urn:" ) && !valueToAssert.startsWith( "urn:ogc:def:crs:" ) ) {
            assertValidUrnCrsIdentifier( valueToAssert, failureMsg );
        } else if ( valueToAssert.startsWith( "urn:ogc:def:crs:" )
                    || valueToAssert.startsWith( "http://www.opengis.net/def/crs/" ) ) {
            assertValidOgcNameCrsIdentifier( valueToAssert, failureMsg );
        } else {
            throw new AssertionError( failureMsg );
        }
    }

    /**
     * Assert that one of the default CRS
     * 
     * <pre>
     *   * http://www.opengis.net/def/crs/OGC/1.3/CRS84 (for coordinates without height)
     *   * http://www.opengis.net/def/crs/OGC/0/CRS84h (for coordinates with height)
     * </pre>
     * 
     * is in the list of passed crs.
     * 
     * @param valueToAssert
     *            list of CRS which should contain the default crs, never <code>null</code>
     * @param failureMsg
     *            the message to throw in case of a failure, should not be <code>null</code>
     */
    public static void assertDefaultCrs( List<String> valueToAssert, String failureMsg ) {
        if ( !valueToAssert.contains( OgcApiFeatures10.DEFAULT_CRS )
             && !valueToAssert.contains( OgcApiFeatures10.DEFAULT_CRS_WITH_HEIGHT ) )
            throw new AssertionError( failureMsg );
    }

    /**
     * @param crsHeaderValue
     *            the value from the header, never <code>null</code>
     * @param expectedValue
     *            th expected value, never <code>null</code>
     * @param failureMsg
     *            the message to throw in case of a failure, should not be <code>null</code>
     */
    public static void assertCrsHeader( String crsHeaderValue, String expectedValue, String failureMsg ) {
        if ( !crsHeaderValue.matches( "<" + expectedValue + ">" ) ) {
            throw new AssertionError( failureMsg );
        }
    }

    /**
     * Assert that one of the default CRS
     *
     * <pre>
     *   * http://www.opengis.net/def/crs/OGC/1.3/CRS84 (for coordinates without height)
     *   * http://www.opengis.net/def/crs/OGC/0/CRS84h (for coordinates with height)
     * </pre>
     * 
     * @param crsHeaderValue
     *            the value from the header, never <code>null</code>
     * @param failureMsg
     *            the message to throw in case of a failure, should not be <code>null</code>
     */
    public static void assertDefaultCrsHeader( String crsHeaderValue, String failureMsg ) {
        if ( !crsHeaderValue.matches( "<" + OgcApiFeatures10.DEFAULT_CRS + ">" )
             && !crsHeaderValue.matches( "<" + OgcApiFeatures10.DEFAULT_CRS_WITH_HEIGHT + ">" ) ) {
            throw new AssertionError( failureMsg );
        }
    }

    private static void assertValidHttpCrsIdentifier( String valueToAssert, String failureMsg ) {
        UrlValidator urlValidator = new UrlValidator();
        if ( !urlValidator.isValid( valueToAssert ) ) {
            throw new AssertionError( failureMsg );
        }
    }

    private static void assertValidUrnCrsIdentifier( String valueToAssert, String failureMsg ) {
        UrnValidator urnValidator = new UrnValidator();
        if ( !urnValidator.isValid( valueToAssert ) ) {
            throw new AssertionError( failureMsg );
        }
    }

    private static void assertValidOgcNameCrsIdentifier( String valueToAssert, String failureMsg ) {
        OgcNameValidator ogcNameValidator = new OgcNameValidator();
        if ( !ogcNameValidator.isValid( valueToAssert ) ) {
            throw new AssertionError( failureMsg );
        }
    }
}