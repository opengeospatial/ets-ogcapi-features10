package org.opengis.cite.ogcapifeatures10.conformance;

/**
 *
 * Encapsulates all known requirement classes.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public enum RequirementClass {

    CORE( "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core" ),

    HTML( "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html", "text/html", "text/html" ),

    GEOJSON( "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson", "application/geo+json", "application/json" ),

    GMLSF0( "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf0",
           "application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0",
           "application/xml" ),

    GMLSF2( "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf2",
           "application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf2",
           "application/xml" ),

    OPENAPI30( "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30" ),

    CRS( "http://www.opengis.net/spec/ogcapi-features-2/1.0/conf/crs" );

    private final String conformanceClass;

    private final String mediaTypeFeaturesAndCollections;

    private final String mediaTypeOtherResources;

    RequirementClass( String conformanceClass ) {
        this( conformanceClass, null, null );
    }

    RequirementClass( String conformanceClass, String mediaTypeFeaturesAndCollections, String mediaTypeOtherResources ) {
        this.conformanceClass = conformanceClass;
        this.mediaTypeFeaturesAndCollections = mediaTypeFeaturesAndCollections;
        this.mediaTypeOtherResources = mediaTypeOtherResources;
    }

    /**
     * @return <code>true</code> if the RequirementClass has a media type for features and collections,
     *         <code>true</code> otherwise
     */
    public boolean hasMediaTypeForFeaturesAndCollections() {
        return mediaTypeFeaturesAndCollections != null;
    }

    /**
     * @return media type for features and collections, <code>null</code> if not available
     */
    public String getMediaTypeFeaturesAndCollections() {
        return mediaTypeFeaturesAndCollections;
    }

    /**
     * @return <code>true</code> if the RequirementClass has a media type for other resources,
     *         <code>true</code> otherwise
     */
    public boolean hasMediaTypeForOtherResources() {
        return mediaTypeOtherResources != null;
    }

    /**
     * @return media type of other resources, <code>null</code> if not available
     */
    public String getMediaTypeOtherResources() {
        return mediaTypeOtherResources;
    }

    /**
     * @param conformanceClass
     *            the conformance class of the RequirementClass to return.
     * @return the RequirementClass with the passed conformance class, <code>null</code> if RequirementClass exists
     */
    public static RequirementClass byConformanceClass( String conformanceClass ) {
        for ( RequirementClass requirementClass : values() ) {
            if ( requirementClass.conformanceClass.equals( conformanceClass ) )
                return requirementClass;
        }
        return null;
    }

}