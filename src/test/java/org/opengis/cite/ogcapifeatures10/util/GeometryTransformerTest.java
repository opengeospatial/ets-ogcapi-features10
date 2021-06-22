package org.opengis.cite.ogcapifeatures10.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeometryTransformerTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    public void test() {
        GeometryTransformer geometryTransformer = new GeometryTransformer( "EPSG:4326", "EPSG:25832" );
        Point pointToTransform = geometryFactory.createPoint( new Coordinate( 7.55, 51.82 ) );
        Point transformedPoint = geometryTransformer.transform( pointToTransform );
        assertEquals( transformedPoint.getX(), 400060.46, 0.01 );
        assertEquals( transformedPoint.getY(), 5742012.57, 0.01 );
    }

}
