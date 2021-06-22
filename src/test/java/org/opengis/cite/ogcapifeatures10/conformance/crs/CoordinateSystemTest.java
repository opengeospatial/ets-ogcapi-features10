package org.opengis.cite.ogcapifeatures10.conformance.crs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;

import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CoordinateSystemTest {

    @Test
    public void testSridFromCrs_CRS84()
                            throws Exception {
        assertThat( DEFAULT_CRS.getSrid(), is( 4326 ) );
    }

    @Test
    public void testSridFromCrs_HTTP()
                            throws Exception {
        CoordinateSystem coordinateSystem = new CoordinateSystem( "http://www.opengis.net/def/crs/EPSG/0/3163" );
        assertThat( coordinateSystem.getSrid(), is( 3163 ) );
    }

    @Test
    public void testSridFromCrs_URN()
                            throws Exception {
        CoordinateSystem coordinateSystem = new CoordinateSystem( "urn:ogc:def:crs:EPSG::3163" );
        assertThat( coordinateSystem.getSrid(), is( 3163 ) );
    }

}
