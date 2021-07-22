package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.features;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertCrsHeader;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.CRS_PARAMETER;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeaturesUrlForGeoJson;

import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.testng.SkipException;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Verifies header in the paths /collection/{collectionId}/items
 * 
 * <pre>
 * Abstract Test 4: /conf/crs/crs-parameter
 * Test Purpose: Verify that the parameter crs has been implemented correctly
 * Requirement: /req/crs/fc-crs-definition, /req/crs/fc-crs-valid-value B, /req/crs/ogc-crs-header, /req/crs/ogc-crs-header-value, /req/crs/geojson
 *
 * Test Method
 * For
 *  * each spatial feature collection collectionId,
 *  * every GML or GeoJSON feature representation supported by the Web API, and
 *  * every CRS supported for the collection (every CRS listed in the crs property of the collection plus those in the global CRS list, if #/crs is included in the crs property)
 * send a request with CRS identifier in the parameter crs to
 *  * /collections/{collectionId}/items and
 *  * /collections/{collectionId}/items/{featureId} (with a valid featureId for the collection).
 * Verify that
 *  * every response is a valid Features or Feature response,
 *  * has the status code 200 and
 *  * includes a Content-Crs http header with the value of the requested CRS identifier.
 * </pre>
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesCrsParameter extends AbstractFeaturesCrs {

    /**
     * Test: Content-Crs header in the path /collections/{collectionId}/items
     *
     * @param collectionId
     *            id id of the collection, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     * @param crs
     *            the crs to test, never <code>null</code>
     */
    @Test(description = "Implements A.2.1 Query, Parameter crs, Abstract Test 4 (Requirement /req/crs/fc-crs-definition, /req/crs/fc-crs-valid-value B, /req/crs/ogc-crs-header, /req/crs/ogc-crs-header-value, /req/crs/geojson), "
                        + "Content-Crs header in the path /collections/{collectionId}/items", dataProvider = "collectionIdAndJsonAndCrs", dependsOnGroups = "crs-conformance", priority = 1)
    public void verifyFeaturesCrsParameter( String collectionId, JsonPath collection, CoordinateSystem crs ) {
        String featuresUrl = findFeaturesUrlForGeoJson( rootUri, collection );
        if ( featuresUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );

        Response response = init().baseUri( featuresUrl ).queryParam( CRS_PARAMETER,
                                                                      crs.getCode() ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );
        String actualHeader = response.getHeader( "Content-Crs" );
        if ( actualHeader == null ) {
            throw new AssertionError( String.format( "Features response at '%s' does not provide the expected header 'Content-Crs'",
                                                     featuresUrl ) );
        }
        assertCrsHeader( actualHeader, crs,
                         String.format( "Features response at '%s' does not provide expected 'Content-Crs' header, was: '%s', expected: '%s'",
                                        featuresUrl, actualHeader, crs ) );
    }

}