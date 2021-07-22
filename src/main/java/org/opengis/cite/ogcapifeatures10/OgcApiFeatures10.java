package org.opengis.cite.ogcapifeatures10;

import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;

/**
 * Contains various constants pertaining to WFS 3.0 specification and related standards.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class OgcApiFeatures10 {

    private OgcApiFeatures10() {
    }

    public static final String OPEN_API_MIME_TYPE = "application/vnd.oai.openapi+json;version=3.0";

    public static final String GEOJSON_MIME_TYPE = "application/geo+json";

    /** Conformance class: CRS **/

    public static final String DEFAULT_CRS_CODE = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    public static final String DEFAULT_CRS_WITH_HEIGHT_CODE = "http://www.opengis.net/def/crs/OGC/1.3/CRS84h";

    public static final CoordinateSystem DEFAULT_CRS = new CoordinateSystem( DEFAULT_CRS_CODE );

    public static final CoordinateSystem DEFAULT_CRS_WITH_HEIGHT = new CoordinateSystem( DEFAULT_CRS_WITH_HEIGHT_CODE );

    public static final String UNSUPPORTED_CRS = "http://www.opengis.net/def/crs/0/unsupported";

    public static final String CRS_PARAMETER = "crs";
}
