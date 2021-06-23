package org.opengis.cite.ogcapifeatures10.util;

import static org.junit.Assert.assertEquals;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeometryTransformerTest {

    private static final CoordinateSystem EPSG_25832 = new CoordinateSystem( "http://www.opengis.net/def/crs/EPSG/0/25832" );

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    public void test_transform_Point() {
        GeometryTransformer geometryTransformer = new GeometryTransformer( DEFAULT_CRS, EPSG_25832 );
        Point pointToTransform = geometryFactory.createPoint( new Coordinate( 7.55, 51.82 ) );
        Point transformedPoint = geometryTransformer.transform( pointToTransform );
        assertEquals( transformedPoint.getX(), 400060.46, 0.01 );
        assertEquals( transformedPoint.getY(), 5742012.57, 0.01 );
    }

    @Test
    public void test_transform_BBox() {
        GeometryTransformer geometryTransformer = new GeometryTransformer( DEFAULT_CRS, EPSG_25832 );
        BBox bbox = new BBox( 7.55, 51.82, 8.11, 52.14, DEFAULT_CRS );
        BBox transformedBbox = geometryTransformer.transform( bbox );
        assertEquals( transformedBbox.getMinX(), 400060.46, 0.01 );
        assertEquals( transformedBbox.getMinY(), 5742012.57, 0.01 );
        assertEquals( transformedBbox.getMaxX(), 439092.40, 0.01 );
        assertEquals( transformedBbox.getMaxY(), 5776983.09, 0.01 );
    }

}
