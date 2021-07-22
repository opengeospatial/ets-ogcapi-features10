package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.feature;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertDefaultCrsHeader;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_CODE;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_WITH_HEIGHT_CODE;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeatureUrlForGeoJson;

import org.testng.SkipException;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Verifies default CRS requesting /collection/{collectionId}/items
 *
 * <pre>
 * Abstract Test 6: /conf/crs/crs-parameter-default
 * Test Purpose: Verify that the default value for parameter crs has been implemented correctly
 * Requirement: /req/crs/fc-crs-default-value, /req/crs/ogc-crs-header, /req/crs/ogc-crs-header-value
 *
 * Test Method
 * For each spatial feature collection, send a request without the crs parameter and verify that the response
 * includes a Content-Crs http header with the value of the default CRS identifier of the collection.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureCrsParameterDefault extends AbstractFeatureCrs {

    /**
     * Test: default CRS requesting /collections/{collectionId}/items
     *
     * @param collectionId
     *            id id of the collection, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     * @param featureId
     *            id id of the feature, never <code>null</code>
     */
    @Test(description = "Implements A.2.1 Query, Parameter crs, Abstract Test 6 (Requirement /req/crs/fc-crs-default-value, /req/crs/ogc-crs-header, /req/crs/ogc-crs-header-value),  "
                        + "Default CRS requesting path /collections/{collectionId}/items/{featureId}", dataProvider = "collectionFeatureId", dependsOnGroups = "crs-conformance", priority = 1)
    public void verifyFeatureCrsParameterDefault( String collectionId, JsonPath collection, String featureId ) {
        String featureUrl = findFeatureUrlForGeoJson( rootUri, collection, featureId );
        if ( featureUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );

        Response response = init().baseUri( featureUrl ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );
        String actualHeader = response.getHeader( "Content-Crs" );
        if ( actualHeader == null ) {
            throw new AssertionError( String.format( "Feature response at '%s' does not provide the expected header 'Content-Crs'",
                                                     featureUrl ) );
        }
        assertDefaultCrsHeader( actualHeader,
                                String.format( "Feature response at '%s' does not provide default 'Content-Crs' header, was: '%s', expected: '%s' or '%s",
                                               featureUrl, actualHeader, DEFAULT_CRS_CODE,
                                               DEFAULT_CRS_WITH_HEIGHT_CODE ) );
    }

}
