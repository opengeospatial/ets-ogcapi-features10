package org.opengis.cite.ogcapifeatures10.util;

/**
 * https://github.com/BruceZu/broken_colored_glass/blob/master/project/src/main/java/URN8141.java
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UrnValidator {

    /**
     * <pre>
     * DIGIT         =  0-9
     * ALPHA         =  A-Z / a-z
     * alphanum      =  ALPHA / DIGIT
     * ldh           = 　alphanum / "-"
     * NID           = (alphanum) 0*30(ldh) (alphanum)
     * </pre>
     */
    private static String NID = "\\p{Alnum}[a-zA-Z0-9\\-]{0,30}\\p{Alnum}";

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
     * NSS = pchar * ( pchar / "/" )
     * </pre>
     * 
     * @see #PCHAR
     */
    private static String NSS = "(" + PCHAR + ")((" + PCHAR + ")|/)*";

    /**
     * <pre>
     * assigned-name = "urn" ":" NID ":" NSS
     * </pre>
     * 
     * The leading scheme (urn:) is case-insensitive
     * 
     * @see <a href="https://en.wikipedia.org/wiki/Uniform_Resource_Name">Wiki: Uniform Resource Name</a>
     */
    private static String ASSIGNED_NAME = "([uU][rR][nN]):(" + NID + "):(" + NSS + ")";

    /**
     * <pre>
     * fragment      =       *( pchar / "/" / "?" )
     * </pre>
     */
    private static String FRAGMENT = "((" + PCHAR + ")|/|\\?)*";

    /**
     * <pre>
     * r-component   = pchar *( pchar / "/" / "?" )
     * q-component   = pchar *( pchar / "/" / "?" )
     * </pre>
     */
    private static String R_Q_COMPONENT = "(" + PCHAR + ")" + FRAGMENT;

    /**
     * <pre>
     * rq-components = [ "?+" r-component ] [ "?=" q-component ]
     * </pre>
     * 
     * The order is not gurantee as the '?' can be used in fragment, and the '+' and '=' can be used in pchar.
     * 
     * @see #R_Q_COMPONENT
     */
    private static String RQ_COMPONENTS = "((\\?\\+)(" + R_Q_COMPONENT + "))?((\\?=)(" + R_Q_COMPONENT + "))?";

    /**
     * <pre>
     * f-component   = fragment
     * namestring    = assigned-name  [ rq-components ]  [ "#" f-component ]
     * </pre>
     */
    private static String URN_RFC8141 = "^" + ASSIGNED_NAME + RQ_COMPONENTS + "(#" + FRAGMENT + ")?$";

    /**
     * Checks if the passed urn is a valid urn according RFC 8141, Section 2
     * (https://tools.ietf.org/html/rfc8141#section-2)
     * 
     * @param urn
     *            the urn to check, <code>null</code> results in a invalid URN.
     * @return <code>true</code> if the urn is valid according to RFC 8141, Section 2, <code>false</code> if the urn is
     *         null, empty or not valid.
     */
    public boolean isValid( String urn ) {
        if ( urn == null )
            return false;
        return urn.matches( URN_RFC8141 );
    }

}
