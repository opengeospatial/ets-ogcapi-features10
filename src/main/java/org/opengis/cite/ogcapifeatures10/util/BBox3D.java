package org.opengis.cite.ogcapifeatures10.util;

import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;

/**
 * <p>
 * BBox class.
 * </p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBox3D extends BBox {

	private final double minZ;

	private final double maxZ;

	/**
	 * <p>
	 * Constructor for BBox.
	 * </p>
	 * @param minX Lower left corner, coordinate axis 1
	 * @param minY Lower left corner, coordinate axis 2
	 * @param minZ Minimum value, coordinate axis 3
	 * @param maxX Upper right corner, coordinate axis 1
	 * @param maxY Upper right corner, coordinate axis 2
	 * @param maxZ Maximum value, coordinate axis 3
	 * @param crs CRS of the bbox, may be <code>null</code>
	 */
	public BBox3D(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, CoordinateSystem crs) {
		super(minX, minY, maxX, maxY, crs);
		this.minZ = minZ;
		this.maxZ = maxZ;
	}

	/**
	 * <p>
	 * Constructor for BBox.
	 * </p>
	 * @param minX Lower left corner, coordinate axis 1
	 * @param minY Lower left corner, coordinate axis 2
	 * @param minZ Minimum value, coordinate axis 3
	 * @param maxX Upper right corner, coordinate axis 1
	 * @param maxY Upper right corner, coordinate axis 2
	 * @param maxZ Maximum value, coordinate axis 3
	 */
	public BBox3D(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this(minX, minY, minZ, maxX, maxY, maxZ, DEFAULT_CRS);
	}

	/**
	 * <p>
	 * Getter for the field <code>minZ</code>.
	 * </p>
	 * @return Minimum value, coordinate axis 3
	 */
	public double getMinZ() {
		return minZ;
	}

	/**
	 * <p>
	 * Getter for the field <code>maxZ</code>.
	 * </p>
	 * @return Maximum value, coordinate axis 3
	 */
	public double getMaxZ() {
		return maxZ;
	}

	/**
	 * <p>
	 * asQueryParameter.
	 * </p>
	 * @return the bbox as query string like '-12,10, 12,20'
	 */
	public String asQueryParameter() {
		StringBuilder sb = new StringBuilder();
		DecimalFormat formatter = formatter();
		sb.append(formatter.format(minX)).append(",");
		sb.append(formatter.format(minY)).append(",");
		sb.append(formatter.format(minZ)).append(",");
		sb.append(formatter.format(maxX)).append(",");
		sb.append(formatter.format(maxY)).append(",");
		sb.append(formatter.format(maxZ));
		return sb.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return asQueryParameter();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BBox3D bBox = (BBox3D) o;
		return Double.compare(bBox.minX, minX) == 0 && Double.compare(bBox.minY, minY) == 0
				&& Double.compare(bBox.minZ, minZ) == 0 && Double.compare(bBox.maxX, maxX) == 0
				&& Double.compare(bBox.maxY, maxY) == 0 && Double.compare(bBox.maxZ, maxZ) == 0;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private DecimalFormat formatter() {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern(PATTERN);
		return df;
	}

}
