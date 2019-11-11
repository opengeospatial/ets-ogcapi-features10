package org.opengis.cite.ogcapifeatures10.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBox {

    private static final String PATTERN = "###.0000000";

    private final double minX;

    private final double minY;

    private final double maxX;

    private final double maxY;

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
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
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

    private DecimalFormat formatter() {
        NumberFormat nf = NumberFormat.getNumberInstance( Locale.ENGLISH );
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern( PATTERN );
        return df;
    }

}