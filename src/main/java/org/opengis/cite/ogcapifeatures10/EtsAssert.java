package org.opengis.cite.ogcapifeatures10;

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