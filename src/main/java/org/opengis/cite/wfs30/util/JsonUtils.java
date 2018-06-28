package org.opengis.cite.wfs30.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Parses all links with 'type' of one of the passed mediaTypes and the 'rel' property with the passed value.
     *
     * @param links
     *            list of all links, never <code>null</code>
     * @param mediaTypesToSupport
     *            a list of media types the links searched for should support, may be empty but never <code>null</code>
     * @param expectedRel
     *            the expected value of the property 'rel', never <code>null</code>
     * @return a list of links supporting one of the media types and with the expected 'rel' property, may be empty but
     *         never <code>null</code>
     */
    public static List<Map<String, Object>> findLinksWithSupportedMediaTypeByRel( List<Map<String, Object>> links,
                                                                                  List<String> mediaTypesToSupport,
                                                                                  String expectedRel ) {
        List<Map<String, Object>> alternateLinks = new ArrayList<>();
        for ( Map<String, Object> link : links ) {
            Object type = link.get( "type" );
            Object rel = link.get( "rel" );
            if ( expectedRel.equals( rel ) && isSupportedMediaType( type, mediaTypesToSupport ) )
                alternateLinks.add( link );
        }
        return alternateLinks;
    }

    /**
     * Parsing the media types which does not have a link woth property 'type' for.
     *
     * @param links
     *            list of links to search in, never <code>null</code>
     * @param mediaTypesToSuppport
     *            a list of media types which should be supported, never <code>null</code>
     * @return the media types which does not have a link for.
     */
    public static List<String> findUnsupportedTypes( List<Map<String, Object>> links, List<String> mediaTypesToSuppport ) {
        List<String> unsupportedType = new ArrayList<>();
        for ( String contentMediaType : mediaTypesToSuppport ) {
            boolean hasLinkForContentType = hasLinkForContentType( links, contentMediaType );
            if ( !hasLinkForContentType )
                unsupportedType.add( contentMediaType );
        }
        return unsupportedType;
    }

    /**
     * Parses the links without 'rel' or 'type' property.
     * 
     * @param links
     *            list of links to search in, never <code>null</code>
     * @return the links without 'rel' or 'type' property
     */
    public static List<String> findLinksWithoutRelOrType( List<Map<String, Object>> links ) {
        List<String> linksWithoutRelOrType = new ArrayList<>();
        for ( Map<String, Object> alternateLink : links ) {
            if ( !linkIncludesRelAndType( alternateLink ) )
                linksWithoutRelOrType.add( (String) alternateLink.get( "href" ) );
        }
        return linksWithoutRelOrType;
    }

    /**
     * Parses the link with 'rel=self'.
     *
     * @param links
     *            list of links to search in, never <code>null</code>
     * @return the link to itself or <code>null</code> if no such link exists
     */
    public static Map<String, Object> findLinkToItself( List<Map<String, Object>> links ) {
        for ( Map<String, Object> link : links ) {
            Object rel = link.get( "rel" );
            if ( "self".equals( rel ) )
                return link;
        }
        return null;
    }

    /**
     * Checks if the passed link contains 'rel' and 'type' properties.
     * 
     * @param link
     *            to check, never <code>null</code>
     * @return <code>true</code> if the link contains 'rel' and 'type' properties, <code>false</code> otherwise
     */
    public static boolean linkIncludesRelAndType( Map<String, Object> link ) {
        Object rel = link.get( "rel" );
        Object type = link.get( "type" );
        if ( rel != null && type != null )
            return true;
        return false;
    }

    private static boolean hasLinkForContentType( List<Map<String, Object>> alternateLinks, String mediaType ) {
        for ( Map<String, Object> alternateLink : alternateLinks ) {
            Object type = alternateLink.get( "type" );
            if ( mediaType.equals( type ) )
                return true;
        }
        return false;
    }

    private static boolean isSupportedMediaType( Object type, List<String> contentMediaTypes ) {
        for ( String contentMediaType : contentMediaTypes ) {
            if ( contentMediaType.equals( type ) )
                return true;
        }
        return false;
    }

}
