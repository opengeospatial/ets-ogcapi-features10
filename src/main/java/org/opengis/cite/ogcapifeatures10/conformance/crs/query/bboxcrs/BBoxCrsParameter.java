package org.opengis.cite.ogcapifeatures10.conformance.crs.query.bboxcrs;

import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseSpatialExtent;

import org.opengis.cite.ogcapifeatures10.util.BBox;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.SkipException;
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

    /**
     * @param collectionId
     *            the id of the collection, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     * @param crs
     *            the crs to test, never <code>null</code>
     */
    @Test(description = "Implements A.2.2 Query, Parameter bbox-crs, Abstract Test 8 (Requirement /req/crs/fc-bbox-crs-definition, /req/crs/bbox-crs-action)", dataProvider = "collectionCrs", dependsOnGroups = "crs-conformance", priority = 1)
    public void verifyBboxCrsParameter( String collectionId, JsonPath collection, String crs ) {
        String featuredUrl = JsonUtils.findFeaturesUrlForGeoJson( rootUri, collection );
        if ( featuredUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        BBox bbox = parseSpatialExtent( collection.get() );
        if ( bbox == null )
            throw new SkipException( "Collection with id " + collectionId + " has no spatial extent" );
        BBox transformedBbox = transformBbox( bbox, crs );
        String bboxParameterValue = transformedBbox.asQueryParameter();

        Response responseWithBBox = init().baseUri( featuredUrl ).param( BBOX_CRS_PARAM,
                                                                         crs ).param( BBOX_PARAM,
                                                                                      bboxParameterValue ).accept( GEOJSON_MIME_TYPE ).when().request( Method.GET );
        responseWithBBox.then().statusCode( 200 );

        Response responseWithoutBBox = init().baseUri( featuredUrl ).accept( GEOJSON_MIME_TYPE ).when().request( Method.GET );
        responseWithoutBBox.then().statusCode( 200 );

        assertSameFeatures( responseWithBBox.jsonPath(), responseWithoutBBox.jsonPath() );
    }

    private BBox transformBbox( BBox bbox, String targetCrs ) {
        // TODO: transform BBox to targetCrs
        return bbox;
    }

}
