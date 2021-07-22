package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs;

import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_CODE;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_WITH_HEIGHT_CODE;

import java.util.Objects;

import org.apache.commons.validator.routines.UrlValidator;
import org.opengis.cite.ogcapifeatures10.exception.UnknownCrsException;
import org.opengis.cite.ogcapifeatures10.util.OgcNameValidator;
import org.opengis.cite.ogcapifeatures10.util.UrnValidator;

/**
 * Encapsulates an CRS from
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CoordinateSystem {

    private final String code;

    /**
     * @param code
     *            the coordinate system, never <code>null</code>
     */
    public CoordinateSystem( String code ) {
        this.code = code;
    }

    /**
     * @return the code of the crs, never <code>null</code>
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the crs as header value (like &lt;CODE&gt;, never <code>null</code>
     */
    public String getAsHeaderValue() {
        return "<" + code + ">";
    }

    /**
     * srid from the passed crs
     *
     * @return the parsed srid, -1 if the crs is <code>null</code>
     * @throws UnknownCrsException
     *             if the srid could not be parsed
     */
    public int getSrid() {
        if ( isDefaultCrs() )
            return 84;
        try {
            if ( code.startsWith( "http://www.opengis.net/def/crs/" ) )
                return Integer.parseInt( code.substring( code.lastIndexOf( "/" ) + 1 ) );
            if ( code.startsWith( "urn:ogc:def:crs:" ) )
                return Integer.parseInt( code.substring( code.lastIndexOf( ":" ) + 1 ) );
        } catch ( NumberFormatException e ) {
            throw new UnknownCrsException( String.format( "Could not parse srid from crs '%s', crs is not supported.",
                                                          code ) );
        }
        throw new UnknownCrsException( String.format( "Could not parse srid from crs '%s', crs is not supported.",
                                                      code ) );
    }

    /**
     * @return the code with the authority: EPSG:CODE, may be <code>null</code>
     * @throws UnknownCrsException
     *             if the crs is not a OGC URN (starting with urn:ogc:def:crs:epsg) or OGC http-URIs (starting with
     *             http://www.opengis.net/def/crs/epsg) with EPSG auhority
     */
    public String getCodeWithAuthority() {
        if ( isDefaultCrs() )
            return "OGC:CRS84";
        if ( code.startsWith( "urn:ogc:def:crs:EPSG" ) || code.startsWith( "http://www.opengis.net/def/crs/EPSG" ) ) {
            int srid = getSrid();
            return String.format( "EPSG:%s", srid );
        }
        throw new UnknownCrsException( String.format( "CRS %s is not supported, only OGC URNs (starting with urn:ogc:def:crs:epsg) and OGC http-URIs (starting with http://www.opengis.net/def/crs/epsg) with EPSG auhority are supported.",
                                                      code ) );
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
     * @return <code>true</code> if the crs is valid, <code>false</code> otherwise
     */
    public boolean isValid() {
        if ( code.startsWith( "http:" ) && !code.startsWith( "http://www.opengis.net/def/crs/" ) ) {
            return assertValidHttpCrsIdentifier( code );
        } else if ( code.startsWith( "https:" ) ) {
            return assertValidHttpCrsIdentifier( code );
        } else if ( code.startsWith( "urn:" ) && !code.startsWith( "urn:ogc:def:crs:" ) ) {
            return assertValidUrnCrsIdentifier( code );
        } else if ( code.startsWith( "urn:ogc:def:crs:" ) || code.startsWith( "http://www.opengis.net/def/crs/" ) ) {
            return assertValidOgcNameCrsIdentifier( code );
        }
        return false;
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;
        CoordinateSystem that = (CoordinateSystem) o;
        return code.equals( that.code );
    }

    @Override
    public int hashCode() {
        return Objects.hash( code );
    }

    private boolean assertValidHttpCrsIdentifier( String valueToAssert ) {
        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid( valueToAssert );
    }

    private boolean assertValidUrnCrsIdentifier( String valueToAssert ) {
        UrnValidator urnValidator = new UrnValidator();
        return urnValidator.isValid( valueToAssert );
    }

    private boolean assertValidOgcNameCrsIdentifier( String valueToAssert ) {
        OgcNameValidator ogcNameValidator = new OgcNameValidator();
        return ogcNameValidator.isValid( valueToAssert );
    }

    private boolean isDefaultCrs() {
        return DEFAULT_CRS_CODE.equals( code ) || DEFAULT_CRS_WITH_HEIGHT_CODE.equals( code );
    }
}
