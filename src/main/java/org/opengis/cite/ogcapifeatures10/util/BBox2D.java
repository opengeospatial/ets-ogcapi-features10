package org.opengis.cite.ogcapifeatures10.util;

import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;

import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;

/**
 * <p>
 * BBox class.
 * </p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBox2D extends BBox {

	/**
	 * <p>
	 * Constructor for BBox.
	 * </p>
	 * @param minX Lower left corner, coordinate axis 1
	 * @param minY Lower left corner, coordinate axis 2
	 * @param maxX Upper right corner, coordinate axis 1
	 * @param maxY Upper right corner, coordinate axis 2
	 */
	public BBox2D(double minX, double minY, double maxX, double maxY) {
		this(minX, minY, maxX, maxY, DEFAULT_CRS);
	}

	/**
	 * <p>
	 * Constructor for BBox.
	 * </p>
	 * @param minX Lower left corner, coordinate axis 1
	 * @param minY Lower left corner, coordinate axis 2
	 * @param maxX Upper right corner, coordinate axis 1
	 * @param maxY Upper right corner, coordinate axis 2
	 * @param crs CRS of the bbox, may be <code>null</code>
	 */
	public BBox2D(double minX, double minY, double maxX, double maxY, CoordinateSystem crs) {
		super(minX, minY, maxX, maxY, crs);
	}

}
