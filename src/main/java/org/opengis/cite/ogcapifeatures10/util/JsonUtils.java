package org.opengis.cite.ogcapifeatures10.util;

import static io.restassured.RestAssured.given;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Parse value ass string.
     *
     * @param value
     *            to parse, may be <code>null</code>
     * @return the value as string, <code>null</code> if the passed value was <code>null</code>
     */
    public static String parseAsString( Object value ) {
        if ( value == null )
            return null;
        return value.toString();
    }

    /**
     * Parses the id of the first feature from the passed json.
     * 
     * @param collectionItemJson
     *            the json document containing the features, never <code>null</code>
     * @return the parsed id, may be <code>null</code> if no feature could be found
     */
    public static String parseFeatureId( JsonPath collectionItemJson ) {
        List<Map<String, Object>> features = collectionItemJson.get( "features" );
        if ( features == null )
            return null;
        for ( Map<String, Object> feature : features ) {
            if ( feature.containsKey( "id" ) )
                return feature.get( "id" ).toString();
        }
        return null;
    }

    /**
     * Parses the temporal extent from the passed collection.
     *
     * @param collection
     *            the collection containing the extent to parse, never <code>null</code>
     * @return the parsed temporal extent, <code>null</code> if no extent exists
     * @throws IllegalArgumentException
     *             if the number of items in the extent invalid
     *
     */
    public static TemporalExtent parseTemporalExtent( Map<String, Object> collection ) {
        Object extent = collection.get( "extent" );
        if ( extent == null || !( extent instanceof Map ) )
            return null;
        Object spatial = ( (Map<String, Object>) extent ).get( "temporal" );
        if ( spatial == null || !( spatial instanceof List ) )
            return null;
        List<Object> coords = (List<Object>) spatial;
        if ( coords.size() == 2 ) {
            ZonedDateTime begin = parseAsDate( (String) coords.get( 0 ) );
            ZonedDateTime end = parseAsDate( (String) coords.get( 1 ) );
            return new TemporalExtent( begin, end );
        }
        throw new IllegalArgumentException( "Temporal extent with " + coords.size() + " items is invalid" );
    }

    /**
     * Parses the passed string as ISO 8601 date.
     * 
     * @param dateTime
     *            the dateTime to parse, never <code>null</code>
     * @return the parsed date, never <code>null</code>
     */
    public static ZonedDateTime parseAsDate( String dateTime ) {
        return ZonedDateTime.parse( dateTime );
    }

    /**
     * Formats the passed string as ISO 8601 date. Example: "2018-02-12T23:20:50Z"
     *
     * @param dateTime
     *            the dateTime to format, never <code>null</code>
     * @return the formatted date, never <code>null</code>
     */
    public static String formatDate( ZonedDateTime dateTime ) {
        return DateTimeFormatter.ISO_INSTANT.format( dateTime );
    }

    /**
     * Formats the passed string as ISO 8601 date. Example: "2018-02-12"
     *
     * @param date
     *            the dateTime to format, never <code>null</code>
     * @return the formatted date, never <code>null</code>
     */
    public static String formatDate( LocalDate date ) {
        return DateTimeFormatter.ISO_DATE.format( date );
    }

    /**
     * Formats the passed string as a period using a start and end time. Example:
     * "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"
     *
     * @param beginDateTime
     *            the begin dateTime to format, never <code>null</code>
     * @param endDateTime
     *            the end dateTime to format, never <code>null</code>
     * @return the formatted date, never <code>null</code>
     */
    public static String formatDateRange( ZonedDateTime beginDateTime, ZonedDateTime endDateTime ) {
        return formatDate( beginDateTime ) + "/" + formatDate( endDateTime );
    }

    /**
     * Formats the passed string as a period using start time and a duration. Example:
     * "2018-02-12T00:00:00Z/P1M6DT12H31M12S"
     *
     * @param beginDate
     *            the begin date to format, never <code>null</code>
     * @param endDate
     *            the end date to format, never <code>null</code>
     * @return the formatted date, never <code>null</code>
     */
    public static String formatDateRangeWithDuration( LocalDate beginDate, LocalDate endDate ) {
        Period betweenDate = Period.between( beginDate, endDate );
        return formatDate( beginDate ) + "/" + betweenDate;
    }

    /**
     * Parses the spatial extent from the passed collection.
     *
     * @param collection
     *            the collection containing the extent to parse, never <code>null</code>
     * @return the parsed bbox, <code>null</code> if no extent exists
     * @throws IllegalArgumentException
     *             if the number of items in the extent invalid
     *
     */
    public static BBox parseSpatialExtent( Map<String, Object> collection ) {
        Object extent = collection.get( "extent" );
        if ( extent == null || !( extent instanceof Map ) )
            return null;
        Object spatial = ( (Map<String, Object>) extent ).get( "spatial" );
        if ( spatial == null || !( spatial instanceof Map ) )
            return null;
        Object bbox = ( (Map<String, Object>) spatial ).get( "bbox" );
        if ( bbox == null || !( bbox instanceof List ) )
            return null;
        List<Object> bboxesOrCoordinates = (List<Object>) bbox;
        if ( bboxesOrCoordinates.isEmpty() )
            return null;
        if ( containsMultipleBboxes( bboxesOrCoordinates ) ) {
            if ( bboxesOrCoordinates.isEmpty() )
                return null;
            Object firstBbox = bboxesOrCoordinates.get( 0 );
            if ( firstBbox == null || !( firstBbox instanceof List ) ) {
                return null;
            }
            List<Object> coordinatesOfFirstBBox = (List<Object>) firstBbox;
            return parseBbox( coordinatesOfFirstBBox, (Map<String, Object>) spatial );
        } else {
            return parseBbox( bboxesOrCoordinates, (Map<String, Object>) spatial );
        }
    }

    private static BBox parseBbox( List<Object> coords, Map<String, Object> spatial ) {
        if ( coords.size() == 4 ) {
            CoordinateSystem crs = parseCrs( spatial );
            double minX = parseValueAsDouble( coords.get( 0 ) );
            double minY = parseValueAsDouble( coords.get( 1 ) );
            double maxX = parseValueAsDouble( coords.get( 2 ) );
            double maxY = parseValueAsDouble( coords.get( 3 ) );
            return new BBox( minX, minY, maxX, maxY, crs );
        } else if ( coords.size() == 6 ) {
            throw new IllegalArgumentException( "BBox with " + coords.size()
                                                + " coordinates is currently not supported" );
        }
        throw new IllegalArgumentException( "BBox with " + coords.size() + " coordinates is invalid" );
    }

    private static CoordinateSystem parseCrs( Map<String, Object> spatial ) {
        String crs = parseAsString( spatial.get( "crs" ) );
        if ( crs != null )
            return new CoordinateSystem( crs );
        return DEFAULT_CRS;
    }

    private static boolean containsMultipleBboxes( List<Object> bboxes ) {
        Object first = bboxes.get( 0 );
        return first instanceof List;
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
    public static List<String> findUnsupportedTypes( List<Map<String, Object>> links,
                                                     List<String> mediaTypesToSuppport ) {
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
     * @param rels
     *            Set of relation types, never <code>null</code>
     * @return the links without 'rel' or 'type' property
     */
    public static List<String> findLinksWithoutRelOrType( List<Map<String, Object>> links, Set<String> rels ) {
        List<String> linksWithoutRelOrType = new ArrayList<>();
        for ( Map<String, Object> link : links ) {
            if ( rels.contains( link.get( "rel" ) ) && !linkIncludesRelAndType( link ) )
                linksWithoutRelOrType.add( (String) link.get( "href" ) );
        }
        return linksWithoutRelOrType;
    }

    /**
     * Parses the link with 'rel=self'.
     *
     * @param links
     *            list of links to search in, never <code>null</code>
     * @param expectedRel
     *            the expected value of the property 'rel', never <code>null</code>
     * @return the link to itself or <code>null</code> if no such link exists
     */
    public static Map<String, Object> findLinkByRel( List<Map<String, Object>> links, String expectedRel ) {
        if ( links == null )
            return null;
        for ( Map<String, Object> link : links ) {
            Object rel = link.get( "rel" );
            if ( expectedRel.equals( rel ) )
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

    /**
     * Checks if a property with the passed name exists in the jsonPath.
     * 
     * @param propertyName
     *            name of the property to check, never <code>null</code>
     * @param jsonPath
     *            to check, never <code>null</code>
     * @return <code>true</code> if the property exists, <code>false</code> otherwise
     */
    public static boolean hasProperty( String propertyName, JsonPath jsonPath ) {
        return jsonPath.get( propertyName ) != null;
    }

    /**
     * Checks if a at least one of the collection in the /collections response has a spatial extent.
     *
     * @param jsonPath
     *            to check, never <code>null</code>
     * @return <code>true</code> at least one of the collection has a spatial extent, <code>false</code> otherwise
     */
    public static boolean hasAtLeastOneSpatialFeatureCollection( JsonPath jsonPath ) {
        List<Object> collections = jsonPath.getList( "collections" );
        for ( Object collectionObj : collections ) {
            if ( hasAtLeastOneSpatialFeatureCollection( (Map<String, Object>) collectionObj ) )
                return true;
        }
        return false;
    }

    /**
     * Checks if a at least one of the collection in the /collections response has a spatial extent.
     *
     * @param collection
     *            to check, never <code>null</code>
     * @return <code>true</code> at least one of the collection has a spatial extent, <code>false</code> otherwise
     */
    public static boolean hasAtLeastOneSpatialFeatureCollection( Map<String, Object> collection ) {
        Object extent = collection.get( "extent" );
        return hasAtLeastOneSpatialFeatureCollection( extent );
    }

    /**
     * Checks if the extent contains a spatial extent.
     *
     * @param extent
     *            to check, never <code>null</code>
     * @return <code>true</code> if extent contains a spatial extent, <code>false</code> otherwise
     */
    public static boolean hasAtLeastOneSpatialFeatureCollection( Object extent ) {
        if ( extent != null && extent instanceof Map ) {
            Object spatial = ( (Map<String, Object>) extent ).get( "spatial" );
            if ( spatial != null )
                return true;
        }
        return false;
    }

    /**
     * Retrieves the property values as list.
     *
     * @param propertyName
     *            name of the property, never <code>null</code>
     * @param jsonPath
     *            the json document to retrieve properties from, never <code>null</code>
     * @return the property values as list, may be empty but never <code>null</code>
     */
    public static List<String> parseAsList( String propertyName, JsonPath jsonPath ) {
        Object value = jsonPath.get( propertyName );
        if ( value == null )
            return Collections.emptyList();
        if ( value instanceof String )
            return Collections.singletonList( (String) value );
        return jsonPath.getList( propertyName );
    }

    /**
     * Retrieves the property values as list.
     *
     * @param propertyName
     *            name of the property, never <code>null</code>
     * @param jsonPath
     *            the json document to retrieve properties from, never <code>null</code>
     * @return the property values as list, may be empty but never <code>null</code>
     */
    public static List<Map<String, Object>> parseAsListOfMaps( String propertyName, JsonPath jsonPath ) {
        Object value = jsonPath.get( propertyName );
        if ( value == null )
            return Collections.emptyList();
        return jsonPath.getList( propertyName );
    }

    /**
     * Retrieves the property values as list.
     *
     * @param propertyName
     *            name of the property, never <code>null</code>
     * @param json
     *            the json map to retrieve properties from, never <code>null</code>
     * @return the property values as list, may be empty but never <code>null</code>
     */
    public static List<String> parseAsList( String propertyName, Map<String, Object> json ) {
        Object value = json.get( propertyName );
        if ( value == null )
            return Collections.emptyList();
        if ( value instanceof String )
            return Collections.singletonList( (String) value );
        return (List<String>) json.get( propertyName );
    }

    /**
     * Collects the number of all returned features by iterating over all 'next' links and summarizing the size of
     * features in 'features' array property.
     * 
     * @param jsonPath
     *            the initial collection, never <code>null</code>
     * @param maximumLimit
     *            the limit parameter value to use, if &lt;= 0 the parameter is omitted
     * @return the number of all returned features
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    public static int collectNumberOfAllReturnedFeatures( JsonPath jsonPath, int maximumLimit )
                            throws URISyntaxException {
        int numberOfAllReturnedFeatures = parseAsList( "features", jsonPath ).size();
        Map<String, Object> nextLink = findLinkByRel( jsonPath.getList( "links" ), "next" );
        while ( nextLink != null ) {
            String nextUrl = (String) nextLink.get( "href" );
            URI uri = new URI( nextUrl );

            RequestSpecification accept = given().log().all().baseUri( nextUrl ).accept( GEOJSON_MIME_TYPE );
            String[] pairs = uri.getQuery().split( "&" );
            String limitParamFromUri = null;
            for ( String pair : pairs ) {
                int idx = pair.indexOf( "=" );
                String key = pair.substring( 0, idx );
                String value = pair.substring( idx + 1 );
                if ( "limit".equals( key ) ) {
                    limitParamFromUri = value;
                } else {
                    accept.param( key, value );
                }
            }
            if ( maximumLimit > 0 ) {
                accept.param( "limit", maximumLimit );
            } else if ( limitParamFromUri != null ) {
                accept.param( "limit", limitParamFromUri );
            }

            Response response = accept.when().request( GET );
            response.then().statusCode( 200 );

            JsonPath nextJsonPath = response.jsonPath();
            int features = parseAsList( "features", nextJsonPath ).size();
            if ( features > 0 ) {
                numberOfAllReturnedFeatures += features;
                nextLink = findLinkByRel( nextJsonPath.getList( "links" ), "next" );
            } else {
                nextLink = null;
            }
        }
        return numberOfAllReturnedFeatures;
    }

    /**
     * Finds the URL to the resource /collections/{collectionId}/items from the path /collections/{collectionId}
     *
     * @param rootUri
     *            never <code>null</code>
     * @param collection
     *            the /collections/{collectionId} response, never <code>null</code>
     * @return the url to the resource /collections/{collectionId}/items or <code>null</code>
     */
    public static String findFeaturesUrlForGeoJson( URI rootUri, JsonPath collection ) {
        List<Object> links = collection.get( "links" );
        return findFeaturesUrlForGeoJson( rootUri, links );
    }

    /**
     * Finds the URL to the resource /collections/{collectionId}/items from the path /collections
     *
     * @param rootUri
     *            never <code>null</code>
     * @param collection
     *            the collection object /collections response, never <code>null</code>
     * @return the url to the resource /collections/{collectionId}/items or <code>null</code>
     */
    public static String findFeaturesUrlForGeoJson( URI rootUri, Map<String, Object> collection ) {
        List<Object> links = (List<Object>) collection.get( "links" );
        return findFeaturesUrlForGeoJson( rootUri, links );
    }

    /**
     * Finds the URL to the resource /collections/{collectionId}/items/{featureId} from the path /collections and
     * creates an valid url to this resource
     *
     * @param rootUri
     *            never <code>null</code>
     * @param collection
     *            the /collections/{collectionId} response, never <code>null</code>
     * @param featureId
     *            id of the feature, never <code>null</code>
     * @return the url to the resource /collections/{collectionId}/items or <code>null</code>
     */
    public static String findFeatureUrlForGeoJson( URI rootUri, JsonPath collection, String featureId ) {
        List<Object> links = collection.get( "links" );
        return findFeatureUrlForGeoJson( rootUri, featureId, links );
    }

    /**
     * Finds the URL to the resource /collections/{collectionId}/items/{featureId} from the path /collections and
     * creates an valid url to this resource
     *
     * @param rootUri
     *            never <code>null</code>
     * @param collection
     *            the collection object /collections response, never <code>null</code>
     * @param featureId
     *            id of the feature, never <code>null</code>
     * @return the url to the resource /collections/{collectionId}/items or <code>null</code>
     */
    public static String findFeatureUrlForGeoJson( URI rootUri, Map<String, Object> collection, String featureId ) {
        List<Object> links = (List<Object>) collection.get( "links" );
        return findFeatureUrlForGeoJson( rootUri, featureId, links );
    }

    /**
     * Parse the geometry property as geometry.
     *
     * @param feature
     *            to parse, never <code>null</code>
     * @param crs
     *            the crs of the geometry, may be <code>null</code>
     * @return the parsed geometry, <code>null</code> if the feature has no geometry property
     * @throws ParseException
     *             if the geometry could not be parsed
     */
    public static Geometry parseFeatureGeometry( Map<String, Object> feature, CoordinateSystem crs )
                            throws ParseException {
        Map<String, Object> geometry = (Map<String, Object>) feature.get( "geometry" );
        if ( geometry == null )
            return null;
        JSONObject jsonObject = new JSONObject( geometry );
        String geomAsString = jsonObject.toJSONString();
        GeoJsonReader geoJsonReader = new GeoJsonReader();
        Geometry parsedGeometry = geoJsonReader.read( geomAsString );
        parsedGeometry.setSRID( crs.getSrid() );
        return parsedGeometry;
    }


    private static String findFeaturesUrlForGeoJson( URI rootUri, List<Object> links ) {
        for ( Object linkObject : links ) {
            Map<String, Object> link = (Map<String, Object>) linkObject;
            Object rel = link.get( "rel" );
            Object type = link.get( "type" );
            if ( "items".equals( rel ) && GEOJSON_MIME_TYPE.equals( type ) ) {
                String url = (String) link.get( "href" );
                if ( !url.startsWith( "http" ) ) {
                    String path = url;
                    if ( null != rootUri.getScheme() && !rootUri.getScheme().isEmpty() )
                        url = rootUri.getScheme() + ":";
                    if ( null != rootUri.getAuthority() && !rootUri.getAuthority().isEmpty() )
                        url = url + "//" + rootUri.getAuthority();
                    url = url + path;
                    if ( null != rootUri.getQuery() && !rootUri.getQuery().isEmpty() )
                        url = url + "?" + rootUri.getQuery();
                    if ( null != rootUri.getFragment() && !rootUri.getFragment().isEmpty() )
                        url = url + "#" + rootUri.getFragment();
                }
                return url;
            }
        }
        return null;
    }

    private static String findFeatureUrlForGeoJson( URI rootUri, String featureId, List<Object> links ) {
        String featuresUrlForGeoJson = findFeaturesUrlForGeoJson( rootUri, links );
        if ( featuresUrlForGeoJson != null ) {
            return createFeatureUrl( featuresUrlForGeoJson, featureId );
        }
        return null;
    }

    private static String createFeatureUrl( String getFeatureUrl, String featureId ) {
        if ( getFeatureUrl.indexOf( "?" ) != -1 ) {
            return getFeatureUrl.substring( 0, getFeatureUrl.indexOf( "?" ) ) + "/" + featureId;
        } else if ( getFeatureUrl.indexOf( "." ) != -1 ) {
            return getFeatureUrl.substring( 0, getFeatureUrl.lastIndexOf( "." ) ) + "/" + featureId
                   + getFeatureUrl.substring( getFeatureUrl.lastIndexOf( "." ) );
        }
        return getFeatureUrl + "/" + featureId;
    }

    private static boolean isSameMediaType( String mediaType1, String mediaType2 ) {
        if ( mediaType1.contains( ";" ) || mediaType2.contains( ";" ) ) {
            // media types are not case sensitive
            String[] components1 = mediaType1.toLowerCase().split( ";" );
            String[] components2 = mediaType2.toLowerCase().split( ";" );
            // type and subtype must match
            if ( !components1[0].trim().equals( components2[0].trim() ) )
                return false;
            Set<String> parameters1 = new HashSet<>();
            Set<String> parameters2 = new HashSet<>();
            // normalize parameter values and compare them
            for ( int i = 1; i < components1.length; i++ ) {
                String parameter = components1[i].trim().replace( "\"", "" );
                if ( !parameter.isEmpty() )
                    parameters1.add( parameter );
            }
            for ( int i = 1; i < components2.length; i++ ) {
                String parameter = components2[i].trim().replace( "\"", "" );
                if ( !parameter.isEmpty() )
                    parameters2.add( parameter );
            }
            if ( parameters1.size() != parameters2.size() )
                return false;
            if ( !parameters1.containsAll( parameters2 ) )
                return false;
        } else if ( !mediaType1.trim().equalsIgnoreCase( mediaType2.trim() ) )
            return false;

        return true;
    }

    private static boolean hasLinkForContentType( List<Map<String, Object>> alternateLinks, String mediaType ) {
        for ( Map<String, Object> alternateLink : alternateLinks ) {
            Object type = alternateLink.get( "type" );
            if ( type instanceof String && isSameMediaType( mediaType, (String) type ) )
                return true;
        }
        return false;
    }

    private static boolean isSupportedMediaType( Object type, List<String> mediaTypes ) {
        for ( String mediaType : mediaTypes ) {
            if ( type instanceof String && isSameMediaType( mediaType, (String) type ) )
                return true;
        }
        return false;
    }

    private static double parseValueAsDouble( Object value ) {
        if ( value instanceof Integer ) {
            return ( (Integer) value ).doubleValue();
        } else if ( value instanceof Float ) {
            return ( (Float) value ).doubleValue();
        } else if ( value instanceof Double ) {
            return (Double) value;
        } else {
            return Double.parseDouble( value.toString() );
        }
    }
}
