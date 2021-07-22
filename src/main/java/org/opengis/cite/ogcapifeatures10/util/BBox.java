package org.opengis.cite.ogcapifeatures10.util;

import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBox {

    private static final String PATTERN = "###.0000000";

    private final double minX;

    private final double minY;

    private final double maxX;

    private final double maxY;

    private final CoordinateSystem crs;

    /**
     * @param minX
     *            Lower left corner, coordinate axis 1
     * @param minY
     *            Lower left corner, coordinate axis 2
     * @param maxX
     *            Upper right corner, coordinate axis 1
     * @param maxY
     *            Upper right corner, coordinate axis 2
     */
    public BBox( double minX, double minY, double maxX, double maxY ) {
        this( minX, minY, maxX, maxY, DEFAULT_CRS );
    }

    /**
     * @param minX
     *            Lower left corner, coordinate axis 1
     * @param minY
     *            Lower left corner, coordinate axis 2
     * @param maxX
     *            Upper right corner, coordinate axis 1
     * @param maxY
     *            Upper right corner, coordinate axis 2
     * @param crs
     *            CRS of the bbox, may be <code>null</code>
     */
    public BBox( double minX, double minY, double maxX, double maxY, CoordinateSystem crs ) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.crs = crs;
    }

    /**
     * @return Lower left corner, coordinate axis 1
     */
    public double getMinX() {
        return minX;
    }

    /**
     * @return Lower left corner, coordinate axis 2
     */
    public double getMinY() {
        return minY;
    }

    /**
     * @return Upper right corner, coordinate axis 1
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * @return Upper right corner, coordinate axis 2
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * @return CRS of the bbox, never <code>null</code>
     */
    public CoordinateSystem getCrs() {
        return crs;
    }

    /**
     * @return the bbox as query string like '-12,10, 12,20'
     */
    public String asQueryParameter() {
        StringBuilder sb = new StringBuilder();
        DecimalFormat formatter = formatter();
        sb.append( formatter.format( minX ) ).append( "," );
        sb.append( formatter.format( minY ) ).append( "," );
        sb.append( formatter.format( maxX ) ).append( "," );
        sb.append( formatter.format( maxY ) );
        return sb.toString();
    }

    @Override
    public String toString() {
        return asQueryParameter();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;
        BBox bBox = (BBox) o;
        return Double.compare( bBox.minX, minX ) == 0 && Double.compare( bBox.minY, minY ) == 0
               && Double.compare( bBox.maxX, maxX ) == 0 && Double.compare( bBox.maxY, maxY ) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash( minX, minY, maxX, maxY );
    }

    private DecimalFormat formatter() {
        NumberFormat nf = NumberFormat.getNumberInstance( Locale.ENGLISH );
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern( PATTERN );
        return df;
    }
}