package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeaturesUrlForGeoJson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * A.2.7. Features {root}/collections/{collectionId}/items - Error Conditions
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesErrorConditions extends AbstractFeatures {

    public static final String INVALID_QUERY_PARAM_VALUE = "unlimited";

    public static final String UNKNOWN_QUERY_PARAM = "unknownQueryParameter";

    @DataProvider(name = "collectionItemUris")
    public Iterator<Object[]> collectionItemUris( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            collectionsData.add( new Object[] { collection } );
        }
        return collectionsData.iterator();
    }

    /**
     * <pre>
     * Abstract Test 13: /ats/core/fc-op
     * Test Purpose: Validate that features can be identified and extracted from a Collection using query parameters.
     * Requirement: /req/core/fc-op
     *
     * Test Method
     *   1. For every feature collection identified in Collections, issue an HTTP GET request to the URL /collections/{collectionId}/items where {collectionId} is the id property for a Collection described in the Collections content.
     *   2. Validate that a document was returned with a status code 200.
     *   3. Validate the contents of the returned document using test /ats/core/fc-response.
     *
     * Repeat these tests using the following parameter tests:
     * Error conditions:
     *  * Query Invalid /ats/core/query-param-invalid
     *  * Query Unknown /ats/core/query-param-unknown
     * </pre>
     *
     * <pre>
     * Abstract Test 20: /ats/core/query-param-invalid
     * Test Purpose: Validate that the API correctly deals with invalid query parameters.
     * Requirement: /req/core/query-param-invalid
     *
     * Test Method
     *   1. Enter an HTTP request with an invalid query parameter.
     *   2. Verify that the API returns the status code 400.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - Error Conditions, Abstract Test 13/20 (Requirement /req/core/query-param-invalid)", groups = "featuresBase", dataProvider = "collectionItemUris", dependsOnGroups = "collections", alwaysRun = true)
    public void validateFeaturesOperation_QueryParamInvalid( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );

        String featuresUrl = findFeaturesUrlForGeoJson( rootUri, collection );
        if ( featuresUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );

        Response response = init().baseUri( featuresUrl ).accept( GEOJSON_MIME_TYPE ).param( "limit",
                                                                                             INVALID_QUERY_PARAM_VALUE ).when().request( GET );
        response.then().statusCode( 400 );
    }

    /**
     * <pre>
     * Abstract Test 13: /ats/core/fc-op
     * Test Purpose: Validate that features can be identified and extracted from a Collection using query parameters.
     * Requirement: /req/core/fc-op
     *
     * Test Method
     *   1. For every feature collection identified in Collections, issue an HTTP GET request to the URL /collections/{collectionId}/items where {collectionId} is the id property for a Collection described in the Collections content.
     *   2. Validate that a document was returned with a status code 200.
     *   3. Validate the contents of the returned document using test /ats/core/fc-response.
     *
     * Repeat these tests using the following parameter tests:
     * Error conditions:
     *  * Query Invalid /ats/core/query-param-invalid
     *  * Query Unknown /ats/core/query-param-unknown
     * </pre>
     *
     * <pre>
     * Abstract Test 21: /ats/core/query-param-unknown
     * Test Purpose: Validate that the API correctly deals with unknown query parameters.
     * Requirement: /req/core/query-param-unknown
     *
     * Test Method
     *   1. Enter an HTTP request with an query parameter that is not specified in the API definition.
     *   2. Verify that the API returns the status code 400.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - Error Conditions, Abstract Test 13/21 (Requirement /req/core/query-param-unknown)", groups = "featuresBase", dataProvider = "collectionItemUris", dependsOnGroups = "collections", alwaysRun = true)
    public void validateFeaturesOperation_QueryParamUnknown( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        boolean freeFormParameterSupported = OpenApiUtils.isFreeFormParameterSupportedForCollection( getApiModel(), iut, collectionId );
        if ( freeFormParameterSupported ) {
            throw new SkipException( "Free-form parameters are supported for collection with id " + collectionId );
        }

        String featuresUrl = findFeaturesUrlForGeoJson( rootUri, collection );
        if ( featuresUrl == null ) {
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        }

        String queryParam = createRandomQueryParam();
        boolean parameterSupportedForCollection = OpenApiUtils.isParameterSupportedForCollection( getApiModel(), iut, collectionId,
                                                                                                  queryParam );
        if ( parameterSupportedForCollection ) {
            throw new SkipException( "Parameters " + queryParam + " is supported for collection with id "
                                     + collectionId );
        }

        Response response = init().baseUri( featuresUrl ).accept( GEOJSON_MIME_TYPE ).param( queryParam,
                                                                                             1 ).when().request( GET );
        response.then().statusCode( 400 );
    }


    private String createRandomQueryParam() {
        Random r = new Random();
        int suffix = 10000 + r.nextInt( 10000 );
        return UNKNOWN_QUERY_PARAM + suffix;
    }

}
