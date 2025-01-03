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

	/**
	 * Constant
	 * <code>OPEN_API_MIME_TYPE="application/vnd.oai.openapi+json;versio"{trunked}</code>
	 */
	public static final String OPEN_API_MIME_TYPE = "application/vnd.oai.openapi+json;version=3.0";

	/** Constant <code>GEOJSON_MIME_TYPE="application/geo+json"</code> */
	public static final String GEOJSON_MIME_TYPE = "application/geo+json";

	/** Conformance class: CRS **/

	public static final String DEFAULT_CRS_CODE = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

	/**
	 * Constant
	 * <code>DEFAULT_CRS_WITH_HEIGHT_CODE="http://www.opengis.net/def/crs/OGC/0/CR"{trunked}</code>
	 */
	public static final String DEFAULT_CRS_WITH_HEIGHT_CODE = "http://www.opengis.net/def/crs/OGC/0/CRS84h";

	/** Constant <code>DEFAULT_CRS</code> */
	public static final CoordinateSystem DEFAULT_CRS = new CoordinateSystem(DEFAULT_CRS_CODE);

	/** Constant <code>DEFAULT_CRS_WITH_HEIGHT</code> */
	public static final CoordinateSystem DEFAULT_CRS_WITH_HEIGHT = new CoordinateSystem(DEFAULT_CRS_WITH_HEIGHT_CODE);

	/**
	 * Constant
	 * <code>UNSUPPORTED_CRS="http://www.opengis.net/def/crs/0/unsupp"{trunked}</code>
	 */
	public static final String UNSUPPORTED_CRS = "http://www.opengis.net/def/crs/0/unsupported";

	/** Constant <code>CRS_PARAMETER="crs"</code> */
	public static final String CRS_PARAMETER = "crs";

	/** Constant <code>PAGING_LIMIT=3</code> */
	public static final int PAGING_LIMIT = 3;

	/** Constant <code>COLLECTIONS_LIMIT=20</code> */
	public static final int COLLECTIONS_LIMIT = 20;

	/** Constant <code>CRS_LIMIT=20</code> */
	public static final int CRS_LIMIT = 20;

	/** Constant <code>FEATURES_LIMIT=100</code> */
	public static final int FEATURES_LIMIT = 100;

	/** Constant <code>NUMBERMATCHED_LIMIT=10000</code> */
	public static final int NUMBERMATCHED_LIMIT = 10000;

}
