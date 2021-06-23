package org.opengis.cite.ogcapifeatures10.conformance.crs.query.bboxcrs;

import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.UNSUPPORTED_CRS;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseSpatialExtent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.util.BBox;
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
 * Abstract Test 9: /conf/crs/bbox-crs-parameter-invalid
 * Test Purpose: Verify that the parameter bbox-crs has been implemented correctly
 * Requirement: /req/crs/fc-bbox-crs-valid-value
 *
 * Test Method
 * For each spatial feature collection collectionId, send a request with the parameters bbox and bbox-crs to /collections/{collectionId}/items with a value for bbox-crs that is not included in the list of CRSs and verify that the response has status code 400.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBoxCrsParameterInvalid extends AbstractBBoxCrs {

    private static final BBox invalidBBox = new BBox( 5, 49, 6, 50 );

    @DataProvider(name = "collectionIdAndJson")
    public Iterator<Object[]> collectionIdAndJson( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            collectionsData.add( new Object[] { collection.getKey(), collection.getValue() } );
        }
        return collectionsData.iterator();
    }

    /**
     * @param collectionId
     *            the id of the collection, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     */
    @Test(description = "Implements A.2.2 Query, Parameter bbox-crs, Abstract Test 9 (Requirement /conf/crs/bbox-crs-parameter-invalid)", dataProvider = "collectionIdAndJson", dependsOnGroups = "crs-conformance", priority = 1)
    public void verifyBboxCrsParameterInvalid( String collectionId, JsonPath collection ) {
        String featuredUrl = JsonUtils.findFeaturesUrlForGeoJson( rootUri, collection );
        if ( featuredUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        BBox bbox = parseSpatialExtent( collection.get() );
        if ( bbox == null )
            throw new SkipException( "Collection with id " + collectionId + " has no spatial extent" );

        Response responseWithBBox = init().baseUri( featuredUrl ).param( BBOX_CRS_PARAM,
                                                                         UNSUPPORTED_CRS ).param( BBOX_PARAM,
                                                                                                  invalidBBox.asQueryParameter() ).accept( GEOJSON_MIME_TYPE ).when().request( Method.GET );
        responseWithBBox.then().statusCode( 400 );
    }

}
