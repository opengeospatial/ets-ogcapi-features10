package org.opengis.cite.ogcapifeatures10.conformance.crs.query.bboxcrs;

import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseSpatialExtent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.opengis.cite.ogcapifeatures10.util.BBox;
import org.opengis.cite.ogcapifeatures10.util.GeometryTransformer;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * <pre>
 * Abstract Test 8: /conf/crs/bbox-crs-parameter
 * Test Purpose: Verify that the parameter bbox-crs has been implemented correctly
 * Requirement: /req/crs/fc-bbox-crs-definition, /req/crs/bbox-crs-action
 *
 * Test Method
 * For every CRS identifier advertized by the Web API that is known to the test engine and for which the test engine can convert geometries between the CRS and the default CRS of the Web API ("known CRS") execute the following test. Skip the test for unknown CRSs.
 *  1. For each spatial feature collection collectionId and every GML or GeoJSON feature representation supported by the Web API, send a request with the parameters bbox and bbox-crs to /collections/{collectionId}/items for every known CRS. Use a bbox value in the spatial extent of the collection, converted to the known CRS. Send the same request, but with no bbox-crs parameter and a bbox value in the default CRS. Do not include a crs parameter in the requests. Verify that the responses include the same features.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBoxCrsParameter extends AbstractBBoxCrs {

    @DataProvider(name = "collectionCrsAndDefaultCrs")
    public Iterator<Object[]> collectionCrs( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            JsonPath json = collection.getValue();
            CoordinateSystem defaultCrs = collectionIdToDefaultCrs.get( collectionId );
            if ( defaultCrs != null ) {
                for ( CoordinateSystem crs : collectionIdToCrs.get( collectionId ) ) {
                    collectionsData.add( new Object[] { collectionId, json, crs, defaultCrs } );
                }
            }
        }
        return collectionsData.iterator();
    }

    /**
     * @param collectionId
     *            the id of the collection, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     * @param crs
     *            the crs to test, never <code>null</code>
     * @param defaultCrs
     *            the defaultCrs of the collection, never <code>null</code>
     */
    @Test(description = "Implements A.2.2 Query, Parameter bbox-crs, Abstract Test 8 (Requirement /req/crs/fc-bbox-crs-definition, /req/crs/bbox-crs-action)", dataProvider = "collectionCrsAndDefaultCrs", dependsOnGroups = "crs-conformance", priority = 1)
    public void verifyBboxCrsParameter( String collectionId, JsonPath collection, CoordinateSystem crs,
                                        CoordinateSystem defaultCrs ) {
        String featuredUrl = JsonUtils.findFeaturesUrlForGeoJson( rootUri, collection );
        if ( featuredUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        BBox bbox = parseSpatialExtent( collection.get() );
        if ( bbox == null )
            throw new SkipException( "Collection with id " + collectionId + " has no spatial extent" );

        Response responseWithBBox = sendRequestWithBBoxAndBBoxCrs( featuredUrl, bbox, crs );
        responseWithBBox.then().statusCode( 200 );

        Response responseWithoutBBox = sendRequestWithBBoxInDefaultCrs( featuredUrl, bbox, defaultCrs );
        responseWithoutBBox.then().statusCode( 200 );

        assertSameFeatures( responseWithBBox.jsonPath(), responseWithoutBBox.jsonPath() );
    }

    private Response sendRequestWithBBoxAndBBoxCrs( String featuredUrl, BBox bbox, CoordinateSystem crs ) {
        GeometryTransformer geometryTransformer = new GeometryTransformer( bbox.getCrs(), crs );
        BBox transformedBbox = geometryTransformer.transform( bbox );
        String bboxParameterValue = transformedBbox.asQueryParameter();

        return init().baseUri( featuredUrl ).param( BBOX_CRS_PARAM,
                                                    crs.getCode() ).param( BBOX_PARAM,
                                                                           bboxParameterValue ).accept( GEOJSON_MIME_TYPE ).when().request( Method.GET );
    }

    private Response sendRequestWithBBoxInDefaultCrs( String featuredUrl, BBox bbox, CoordinateSystem defaultCrs ) {
        GeometryTransformer geometryTransformerToDefaultCrs = new GeometryTransformer( bbox.getCrs(), defaultCrs );
        BBox transformedBboxInDefaultCrs = geometryTransformerToDefaultCrs.transform( bbox );
        String bboxParameterValueDefaulCrs = transformedBboxInDefaultCrs.asQueryParameter();
        return init().baseUri( featuredUrl ).accept( GEOJSON_MIME_TYPE ).param( BBOX_PARAM,
                                                                                bboxParameterValueDefaulCrs ).when().request( Method.GET );
    }

}
