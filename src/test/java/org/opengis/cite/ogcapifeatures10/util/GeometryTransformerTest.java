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

    private static final CoordinateSystem EPSG_4326 = new CoordinateSystem( "http://www.opengis.net/def/crs/EPSG/0/4326" );

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    public void test_transform_Point_CRS84_4326() {
        GeometryTransformer geometryTransformer = new GeometryTransformer( DEFAULT_CRS, EPSG_4326 );
        Point pointToTransform = geometryFactory.createPoint( new Coordinate( 7.55, 51.82 ) );
        Point transformedPoint = geometryTransformer.transform( pointToTransform );
        assertEquals( 51.82, transformedPoint.getX(), 0.01 );
        assertEquals( 7.55, transformedPoint.getY(), 0.01 );
    }

    @Test
    public void test_transform_Point_CRS84_25832() {
        GeometryTransformer geometryTransformer = new GeometryTransformer( DEFAULT_CRS, EPSG_25832 );
        Point pointToTransform = geometryFactory.createPoint( new Coordinate( 7.55, 51.82 ) );
        Point transformedPoint = geometryTransformer.transform( pointToTransform );
        assertEquals( 400060.46, transformedPoint.getX(), 0.01 );
        assertEquals( 5742012.57, transformedPoint.getY(), 0.01 );
    }

    @Test
    public void test_transform_Point_4326_25832() {
        GeometryTransformer geometryTransformer = new GeometryTransformer( EPSG_4326, EPSG_25832 );
        Point pointToTransform = geometryFactory.createPoint( new Coordinate( 51.82, 7.55 ) );
        Point transformedPoint = geometryTransformer.transform( pointToTransform );
        assertEquals( 400060.46, transformedPoint.getX(), 0.01 );
        assertEquals( 5742012.57, transformedPoint.getY(), 0.01 );
    }

    @Test
    public void test_transform_Point_25832_4326() {
        GeometryTransformer geometryTransformer = new GeometryTransformer( EPSG_25832, EPSG_4326 );
        Point pointToTransform = geometryFactory.createPoint( new Coordinate( 400060.46, 5742012.57 ) );
        Point transformedPoint = geometryTransformer.transform( pointToTransform );
        assertEquals( 51.82, transformedPoint.getX(), 0.01 );
        assertEquals( 7.55, transformedPoint.getY(), 0.01 );
    }

    @Test
    public void test_transform_Point_25832_CRS84() {
        GeometryTransformer geometryTransformer = new GeometryTransformer( EPSG_25832, DEFAULT_CRS );
        Point pointToTransform = geometryFactory.createPoint( new Coordinate( 400060.46, 5742012.57 ) );
        Point transformedPoint = geometryTransformer.transform( pointToTransform );
        assertEquals( 7.55, transformedPoint.getX(), 0.01 );
        assertEquals( 51.82, transformedPoint.getY(), 0.01 );
    }

    @Test
    public void test_transform_BBox() {
        GeometryTransformer geometryTransformer = new GeometryTransformer( DEFAULT_CRS, EPSG_25832 );
        BBox bbox = new BBox( 7.55, 51.82, 8.11, 52.14, DEFAULT_CRS );
        BBox transformedBbox = geometryTransformer.transform( bbox );
        assertEquals( 400060.46, transformedBbox.getMinX(), 0.01 );
        assertEquals( 5742012.57, transformedBbox.getMinY(), 0.01 );
        assertEquals( 439092.40, transformedBbox.getMaxX(), 0.01 );
        assertEquals( 5776983.09, transformedBbox.getMaxY(), 0.01 );
    }
}
