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
 * Abstract Test 10: /conf/crs/bbox-crs-parameter-default
 * Test Purpose: Verify that the parameter bbox-crs has been implemented correctly
 * Requirement: /req/crs/fc-bbox-crs-default-value
 *
 * Test Method
 * For each spatial feature collection collectionId and every GML or GeoJSON feature representation supported by the
 * Web API, send a request with the parameters bbox and bbox-crs to /collections/{collectionId}/items for the default CRS
 * of the collection. Use a bbox value in the spatial extent of the collection. Send the same request, but with no
 * bbox-crs parameter. Do not include a crs parameter in the requests. Verify that the responses include the same features.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBoxCrsParameterDefault extends AbstractBBoxCrs {

    @DataProvider(name = "collectionDefaultCrs")
    public Iterator<Object[]> collectionDefaultCrs( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            JsonPath json = collection.getValue();
            CoordinateSystem defaultCrs = collectionIdToDefaultCrs.get( collectionId );
            if ( defaultCrs != null ) {
                collectionsData.add( new Object[] { collectionId, json, defaultCrs } );
            }
        }
        return collectionsData.iterator();
    }

    /**
     * @param collectionId
     *            the id of the collection, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     * @param defaultCrs
     *            the defaultCrs of the collection, never <code>null</code>
     */
    @Test(description = "Implements A.2.2 Query, Parameter bbox-crs, Abstract Test 10 (Requirement /req/crs/fc-bbox-crs-default-value)", dataProvider = "collectionDefaultCrs", dependsOnGroups = "crs-conformance", priority = 1)
    public void verifyBboxCrsParameterDefault( String collectionId, JsonPath collection, CoordinateSystem defaultCrs ) {
        String featuredUrl = JsonUtils.findFeaturesUrlForGeoJson( rootUri, collection );
        if ( featuredUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        BBox bbox = parseSpatialExtent( collection.get() );
        if ( bbox == null )
            throw new SkipException( "Collection with id " + collectionId + " has no spatial extent" );

        GeometryTransformer geometryTransformer = new GeometryTransformer( bbox.getCrs(), defaultCrs );
        BBox transformedBbox = geometryTransformer.transform( bbox );
        String bboxParameterValue = transformedBbox.asQueryParameter();

        Response responseWithBBox = init().baseUri( featuredUrl ).param( BBOX_CRS_PARAM,
                                                                         defaultCrs.getCode() ).param( BBOX_PARAM,
                                                                                                       bboxParameterValue ).accept( GEOJSON_MIME_TYPE ).when().request( Method.GET );
        responseWithBBox.then().statusCode( 200 );

        Response responseWithoutBBox = init().baseUri( featuredUrl ).param( BBOX_PARAM,
                                                                            bboxParameterValue ).accept( GEOJSON_MIME_TYPE ).when().request( Method.GET );
        responseWithoutBBox.then().statusCode( 200 );

        assertSameFeatures( responseWithBBox.jsonPath(), responseWithoutBBox.jsonPath() );
    }

}
