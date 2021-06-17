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
 * Abstract Test 10: /conf/crs/bbox-crs-parameter-default
 * Test Purpose: Verify that the parameter bbox-crs has been implemented correctly
 * Requirement: /req/crs/fc-bbox-crs-default-value
 *
 * Test Method
 * For each spatial feature collection collectionId and every GML or GeoJSON feature representation supported by the Web API, send a request with the parameters bbox and bbox-crs to /collections/{collectionId}/items for the default CRS of the collection. Use a bbox value in the spatial extent of the collection. Send the same request, but with no bbox-crs parameter. Do not include a crs parameter in the requests. Verify that the responses include the same features.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBoxCrsParameterDefault extends AbstractBBoxCrs {

    /**
     * @param collectionId
     *            the id of the collection, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     * @param defaultCrs
     *            the defaultCrs of the collection, never <code>null</code>
     */
    @Test(description = "Implements A.2.2 Query, Parameter bbox-crs, Abstract Test 10 (Requirement /req/crs/fc-bbox-crs-default-value)", dataProvider = "collectionDefaultCrs", dependsOnGroups = "crs-conformance")
    public void verifyBboxCrsParameter( String collectionId, JsonPath collection, String defaultCrs ) {
        String featuredUrl = JsonUtils.findFeaturesUrlForGeoJson( rootUri, collection );
        if ( featuredUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        BBox bbox = parseSpatialExtent( collection.get() );
        if ( bbox == null )
            throw new SkipException( "Collection with id " + collectionId + " has no spatial extent" );
        BBox transformedBbox = transformBbox( bbox, defaultCrs );
        String bboxParameterValue = transformedBbox.asQueryParameter();

        Response responseWithBBox = init().baseUri( featuredUrl ).param( BBOX_CRS_PARAM,
                                                                         defaultCrs ).param( BBOX_PARAM,
                                                                                             bboxParameterValue ).accept( GEOJSON_MIME_TYPE ).when().request( Method.GET );
        responseWithBBox.then().statusCode( 200 );

        Response responseWithoutBBox = init().baseUri( featuredUrl ).param( BBOX_PARAM,
                                                                            bboxParameterValue ).accept( GEOJSON_MIME_TYPE ).when().request( Method.GET );
        responseWithoutBBox.then().statusCode( 200 );

        assertSameFeatures( responseWithBBox.jsonPath(), responseWithoutBBox.jsonPath() );
    }

    private BBox transformBbox( BBox bbox, String targetCrs ) {
        // TODO: transform BBox to targetCrs
        return bbox;
    }

}
