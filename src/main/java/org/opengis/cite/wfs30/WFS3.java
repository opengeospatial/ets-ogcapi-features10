package org.opengis.cite.wfs30;

/**
 * Contains various constants pertaining to WFS 3.0 specification and related standards.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class WFS3 {

    private WFS3() {
    }

    public static final String OPEN_API_MIME_TYPE = "application/openapi+json;version=3.0";

    public static final String GEOJSON_MIME_TYPE = "application/geo+json";

    public enum PATH {

        API( "api" ), CONFORMANCE( "conformance" ), COLLECTIONS( "collections" );

        private String pathItem;

        PATH( String pathItem ) {

            this.pathItem = pathItem;
        }

        public String getPathItem() {
            return pathItem;
        }
    }
}
