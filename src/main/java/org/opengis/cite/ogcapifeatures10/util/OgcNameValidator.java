package org.opengis.cite.ogcapifeatures10.util;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class OgcNameValidator {

    /**
     * <pre>
     * unreserved  =  ALPHA / DIGIT / "-" / "." / "_" / "~"
     * HEXDIG      =  DIGIT / "A" / "B" / "C" / "D" / "E" / "F"
     * pct-encoded =  "%" HEXDIG HEXDIG　;
     * sub-delims  =  "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
     * pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
     * </pre>
     */
    private static String PCHAR = "[a-zA-Z0-9\\-\\._~!\\$&'\\(\\)\\*\\+,;=:@]|(%[0-9A-F]{2})";

    /**
     * <pre>
     * unreserved  =  ALPHA / DIGIT / "-" / "." / "_" / "~"
     * HEXDIG      =  DIGIT / "A" / "B" / "C" / "D" / "E" / "F"
     * pct-encoded =  "%" HEXDIG HEXDIG　;
     * sub-delims  =  "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
     * pchar = unreserved / pct-encoded / sub-delims / "@"
     * </pre>
     */
    private static String PCHAR_NC = "[a-zA-Z0-9\\-\\._~!\\$&'\\(\\)\\*\\+,;=@]|(%[0-9A-F]{2})";

    /**
     * <pre>
     * code          = segment-nz-nc *( "/" segment-nz-nc )
     * </pre>
     */
    private static String CODE = "(" + PCHAR_NC + ")+" + "(/(" + PCHAR_NC + ")+)*";

    /**
     * <pre>
     * codeURN       = segment-nz-nc *( ":" segment-nz-nc )
     * </pre>
     */
    private static String CODE_URN = "(" + PCHAR_NC + ")+" + "(:(" + PCHAR_NC + ")+)*";

    /**
     * <pre>
     * authority     = segment-nz-nc ; a token from the register of OGC authorities2
     * </pre>
     */
    private static String VERSION = "(" + PCHAR_NC + ")+";

    /**
     * <pre>
     * authority     = segment-nz-nc ; a token from the register of OGC authorities2
     * </pre>
     */
    private static String VERSION_URN = "(" + PCHAR_NC + ")*";

    /**
     * <pre>
     * authority     = segment-nz-nc ; a token from the register of OGC authorities2
     * </pre>
     */
    private static String AUTHORIY = "(" + PCHAR_NC + ")+";

    /**
     * <pre>
     * definition-type = segment-nz-nc ; a token from the register of OGC definition types1
     * </pre>
     */
    private static String DEFINITION_TYPE = "crs";

    /**
     * <pre>
     * ResourceSpecificPath = definition-type "/" authority "/" version "/" code
     * </pre>
     */
    private static String RESOURCE_SPECIFIC_PATH = DEFINITION_TYPE + "/" + AUTHORIY + "/" + VERSION + "/" + CODE;

    /**
     * <pre>
     * ResourceSpecificString = definition-type ":" authority ":" versionURN ":" codeURN
     * </pre>
     */
    private static String RESOURCE_SPECIFIC_URN = DEFINITION_TYPE + ":" + AUTHORIY + ":" + VERSION_URN + ":" + CODE_URN;

    /**
     * <pre>
     * OGCResource   = "def"
     * URI           = "http://www.opengis.net/" OGCResource "/" ResourceSpecificPath
     * </pre>
     */
    private static String OGC_NAME_URI = "http://www.opengis.net/def/" + RESOURCE_SPECIFIC_PATH;

    /**
     * <pre>
     * OGCResource   = "def"
     * URN           = "urn:ogc:" OGCResource ":" ResourceSpecificString
     * </pre>
     */
    private static String OGC_NAME_URN = "urn:ogc:def:" + RESOURCE_SPECIFIC_URN;

    /**
     * Checks if the passed urn is a valid urn according OGC Name Type Specification - definitions - part 1 – basic name
     * (https://docs.opengeospatial.org/pol/09-048r5.html)
     * 
     * @param urn
     *            the urn to check, <code>null</code> results in a invalid URN.
     * @return <code>true</code> if the urn is valid according to OGC Name Type Specification - definitions - part 1 –
     *         basic name, <code>false</code> if the urn is null, empty or not valid.
     */
    public boolean isValid( String urn ) {
        if ( urn == null )
            return false;
        return urn.matches( OGC_NAME_URI ) || urn.matches( OGC_NAME_URN );
    }

}
