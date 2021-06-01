package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.CRS_PARAMETER;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.FeaturesInvalidCrsParameter.UNSUPPORTED_CRS;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeatureUrlForGeoJson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.CommonFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Verifies invalid CRS requesting /collection/{collectionId}/items
 *
 * <pre>
 * Abstract Test 5: /conf/crs/crs-parameter-invalid
 * Test Purpose: Verify that invalid values in the parameter crs are reported
 * Requirement: /req/crs/fc-crs-valid-value
 *
 * Test Method
 * For
 *  * each spatial feature collection collectionId
 * send a request with an unsupported CRS identifier in the parameter crs to
 *  * /collections/{collectionId}/items and
 *  * /collections/{collectionId}/items/{featureId} (with a valid featureId for the collection).
 * Verify that the response has status code 400.
 * Unsupported CRS identifiers are all strings not included in the crs property of the collection and also not included in the global CRS list, if #/crs is included in the crs property.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureInvalidCrsParameter extends CommonFixture {

    @DataProvider(name = "collectionFeatureId")
    public Iterator<Object[]> collectionFeatureId( ITestContext testContext ) {
        Map<String, JsonPath> collectionsResponses = (Map<String, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_TO_ID.getName() );
        Map<String, String> collectionNameToFeatureId = (Map<String, String>) testContext.getSuite().getAttribute( SuiteAttribute.FEATUREIDS.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            if ( collectionNameToFeatureId != null && collectionNameToFeatureId.containsKey( collectionId ) ) {
                String featureId = collectionNameToFeatureId.get( collectionId );
                JsonPath json = collection.getValue();
                collectionsData.add( new Object[] { collectionId, json, featureId } );
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
     */
    @Test(description = "Implements A.2.1 Query, Parameter crs, Abstract Test 1 (Requirement /req/crs/fc-crs-valid-value), "
                        + "Invalid CRS requesting path /collections/{collectionId}/items/{featureId}", dataProvider = "collectionFeatureId", dependsOnGroups = "crs-conformance")
    public void verifyFeatureInvalidCrs( String collectionId, JsonPath collection, String featureId ) {
        String featureUrl = findFeatureUrlForGeoJson( rootUri, collection, featureId );
        if ( featureUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );

        Response response = init().baseUri( featureUrl ).queryParam( CRS_PARAMETER,
                                                                     UNSUPPORTED_CRS ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 400 );
    }

}
