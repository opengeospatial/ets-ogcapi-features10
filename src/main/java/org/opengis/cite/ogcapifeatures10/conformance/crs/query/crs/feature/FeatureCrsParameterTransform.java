package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.feature;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertDefaultCrs;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.CRS_PARAMETER;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_CODE;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_WITH_HEIGHT_CODE;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeatureUrlForGeoJson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.opengis.cite.ogcapifeatures10.util.GeometryTransformer;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Verifies header in the paths /collection/{collectionId}/items/{featureId}
 *
 * <pre>
 * Abstract Test 7: /conf/crs/crs-parameter-transform
 * Test Purpose: Verify that the geometries are transformed
 * Requirement: /req/crs/crs-action
 *
 * Test Method
 * For every CRS identifier advertized by the Web API that is known to the test engine and for which the test engine can
 * convert geometries between the CRS and the default CRS of the Web API ("known CRS") execute the following test.
 * Skip the test for unknown CRSs.
 *  1. For each spatial feature collection collectionId, send a request with the parameter crs
 *     to /collections/{collectionId}/items and /collections/{collectionId}/items/{featureId}
 *     (with a valid featureId for the collection) for every known CRS listed. In addition, send the same request,
 *     but without the crs parameter.
 *  2. Convert the response for the known CRS to the default CRS and verify that the responses match. Due to the use of
 *     different coordinate conversions in the test engine and by the API, there will not be an exact match and the test
 *     engine will have to allow for reasonable differences when assessing whether the geometries match.
 * </pre>
 *
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureCrsParameterTransform extends AbstractFeatureCrs {

    private MultiKeyMap collectionIdAndFeatureIdToGeometry = new MultiKeyMap();

    @DataProvider(name = "collectionFeatureIdCrsAndDefaultCrs")
    public Iterator<Object[]> collectionFeatureIdCrsAndDefaultCrs( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            String featureId = collectionIdToFeatureId.get( collectionId );
            CoordinateSystem defaultCrs = collectionIdToDefaultCrs.get( collectionId );
            JsonPath json = collection.getValue();
            for ( CoordinateSystem crs : collectionIdToCrs.get( collectionId ) ) {
                collectionsData.add( new Object[] { collectionId, json, featureId, crs, defaultCrs } );
            }
        }
        return collectionsData.iterator();
    }

    /**
     * Test: invalid CRS requesting /collections/{collectionId}/items
     *
     * @param collectionId
     *            id id of the collection, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     * @param featureId
     *            id id of the feature, never <code>null</code>
     * @throws ParseException
     *             if the geometry could not be parsed
     */
    @Test(description = "Implements A.2.1 Query, Parameter crs, Abstract Test 7 (Requirement /req/crs/crs-action), "
                        + "Geometries in the path /collections/{collectionId}/items/{featureId}", dataProvider = "collectionFeatureId", dependsOnGroups = "crs-conformance", priority = 1)
    public void verifyFeatureCrsParameterTransformWithCrsParameter( String collectionId, JsonPath collection,
                                                                    String featureId )
                            throws ParseException {
        String featureUrl = findFeatureUrlForGeoJson( rootUri, collection, featureId );
        if ( featureUrl == null )
            throw new SkipException( String.format( "Could not find url for collection with id %s supporting GeoJson (type %s)",
                                                    collectionId, GEOJSON_MIME_TYPE ) );

        Response response = init().baseUri( featureUrl ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );
        String crsHeader = response.getHeader( "Content-Crs" );
        if ( crsHeader == null ) {
            throw new AssertionError( String.format( "Feature response at '%s' does not provide the expected header 'Content-Crs'",
                                                     featureUrl ) );
        }
        assertDefaultCrs( crsHeader,
                          String.format( "Feature response at '%s' does not provide default 'Content-Crs' header, was: '%s', expected: '%s' or '%s'",
                                         featureUrl, crsHeader, DEFAULT_CRS_CODE, DEFAULT_CRS_WITH_HEIGHT_CODE ) );
        String crs = crsHeader.substring( 1, crsHeader.length() - 1 );

        Map<String, Object> feature = response.jsonPath().get();
        Geometry geometry = JsonUtils.parseFeatureGeometry( feature, new CoordinateSystem( crs ) );
        collectionIdAndFeatureIdToGeometry.put( collectionId, featureId, geometry );
    }

    /**
     * Test: Content-Crs header in the path /collections/{collectionId}/items/{featureId}
     *
     * @param collectionId
     *            id id of the collection, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     * @param featureId
     *            id id of the feature, never <code>null</code>
     * @param crs
     *            the crs to test, never <code>null</code>
     * @param defaultCRS
     *            the defaultCRS of the collection, never <code>null</code>
     * @throws ParseException
     *             if the geometry could not be parsed
     */
    @Test(description = "Implements A.2.1 Query, Parameter crs, Abstract Test 7 (Requirement /req/crs/crs-action), "
                        + "Transformed geometries in the path /collections/{collectionId}/items/{featureId}", dataProvider = "collectionFeatureIdCrsAndDefaultCrs", dependsOnGroups = "crs-conformance", dependsOnMethods = "verifyFeatureCrsParameterTransformWithCrsParameter", priority = 1)
    public void verifyFeatureCrsParameterTransformWithoutCrsParameter( String collectionId, JsonPath collection,
                                                                       String featureId, CoordinateSystem crs,
                                                                       CoordinateSystem defaultCRS )
                            throws ParseException {
        String featureUrl = findFeatureUrlForGeoJson( rootUri, collection, featureId );
        if ( featureUrl == null )
            throw new SkipException( String.format( "Could not find url for collection with id %s supporting GeoJson (type %s)",
                                                    collectionId, GEOJSON_MIME_TYPE ) );

        Response response = init().baseUri( featureUrl ).queryParam( CRS_PARAMETER,
                                                                     crs.getCode() ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );
        Map<String, Object> feature = response.jsonPath().get();
        Geometry geometry = JsonUtils.parseFeatureGeometry( feature, crs );
        Geometry geometryInDefaultCrs = (Geometry) collectionIdAndFeatureIdToGeometry.get( collectionId, featureId );
        GeometryTransformer geometryTransformer = new GeometryTransformer( crs, defaultCRS );
        Geometry transformedGeometry = geometryTransformer.transform( geometry );
        geometryInDefaultCrs.equalsExact( transformedGeometry, 0.001 );
    }

}
