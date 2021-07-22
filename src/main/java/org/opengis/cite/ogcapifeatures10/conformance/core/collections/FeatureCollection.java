package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.COLLECTIONS;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.IUT;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollectionMetadata;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.CommonDataFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.opengis.cite.ogcapifeatures10.openapi3.UriBuilder;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * A.2.6. Feature Collection {root}/collections/{collectionId}
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureCollection extends CommonDataFixture {

    private Map<String, Response> collectionIdAndResponse = new HashMap<>();

    @DataProvider(name = "collections")
    public Object[][] collections( ITestContext testContext ) {
        List<Map<String, Object>> testPointAndCollections = (List<Map<String, Object>>) testContext.getSuite().getAttribute( COLLECTIONS.getName() );
        int length = testPointAndCollections.size();
        Object[][] objects = new Object[length][];
        int i = 0;
        for ( Map<String, Object> collection : testPointAndCollections )
            objects[i++] = new Object[] { collection };
        return objects;
    }

    @AfterClass
    public void storeCollectionInTestContext( ITestContext testContext ) {
        Map<String, JsonPath> collectionsResponses = new HashMap<>();
        for ( Map.Entry<String, Response> collectionIdAndResponseEntry : collectionIdAndResponse.entrySet() ) {
            if ( collectionIdAndResponseEntry.getValue() != null ) {
                JsonPath jsonPath = collectionIdAndResponseEntry.getValue().jsonPath();
                collectionsResponses.put( collectionIdAndResponseEntry.getKey(), jsonPath );
            }
        }
        testContext.getSuite().setAttribute( SuiteAttribute.COLLECTION_BY_ID.getName(), collectionsResponses );
    }

    /**
     * <pre>
     * Abstract Test 11: /ats/core/sfc-md-op
     * Test Purpose: Validate that the Collection content can be retrieved from the expected location.
     * Requirement: /req/core/sfc-md-op
     *
     * Test Method: For every Feature Collection described in the Collections content, issue an HTTP GET request to the URL /collections/{collectionId} where {collectionId} is the id property for the collection. Validate that a Collection was returned with a status code 200. Validate the contents of the returned document using test /ats/core/sfc-md-success.
     * </pre>
     *
     * @param testContext
     *            never <code>null</code>
     * @param collection
     *            the collection to test, never <code>null</code>
     */
    @Test(description = "Implements A.2.6. Feature Collection {root}/collections/{collectionId}, Abstract Test 11 (Requirement /req/core/sfc-md-op)", groups = "collection", dataProvider = "collections", dependsOnGroups = { "collections" })
    public void validateFeatureCollectionMetadataOperation( ITestContext testContext, Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        assertNotNull( collectionId, "Id of the collection is not available" );
        URI iut = (URI) testContext.getSuite().getAttribute( IUT.getName() );
        List<TestPoint> testPointsForNamedCollection = retrieveTestPointsForCollectionMetadata( getApiModel(), iut,
                                                                                                collectionId );
        if ( testPointsForNamedCollection.isEmpty() )
            throw new SkipException( "Could not find collection with id " + collectionId + " in the OpenAPI document" );

        TestPoint testPoint = testPointsForNamedCollection.get( 0 );
        String testPointUri = new UriBuilder( testPoint ).collectionName( collectionId ).buildUrl();
        Response response = init().baseUri( testPointUri ).accept( JSON ).when().request( GET );
        response.then().statusCode( 200 );

        collectionIdAndResponse.put( collectionId, response );
    }

    /**
     * <pre>
     * Abstract Test 12: /ats/core/sfc-md-success
     * Test Purpose: Validate that the Collection content complies with the required structure and contents.
     * Requirement: /req/core/sfc-md-success
     *
     * Test Method: Verify that the content of the response is consistent with the content for this Feature Collection in the /collections response. That is, the values for id, title, description and extent are identical.
     * </pre>
     *
     * @param collection
     *            the collection to test, never <code>null</code>
     */
    @Test(description = "Implements A.2.6. Feature Collection {root}/collections/{collectionId}, Abstract Test 12 (Requirement /req/core/sfc-md-success)", groups = "collection", dataProvider = "collections", dependsOnMethods = "validateFeatureCollectionMetadataOperation", alwaysRun = true)
    public void validateFeatureCollectionMetadataResponse( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        assertNotNull( collectionId, "Id of the collection is not available" );
        Response response = collectionIdAndResponse.get( collectionId );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        JsonPath jsonPath = response.jsonPath();

        assertEqualStringContent( collection, jsonPath, "id" );
        assertEqualStringContent( collection, jsonPath, "title" );
        assertEqualStringContent( collection, jsonPath, "description" );
        assertExtent( collection, jsonPath );

    }

    private void assertEqualStringContent( Map<String, Object> collection, JsonPath jsonPath, String property ) {
        String idFromCollections = (String) collection.get( property );
        String idFromCollection = jsonPath.get( property );
        assertEquals( idFromCollection, idFromCollections,
                      property + " from collection is not equal to the collections " + property );
    }

    private void assertExtent( Map<String, Object> collection, JsonPath jsonPath ) {
        Map<String, Object> idFromCollections = (Map<String, Object>) collection.get( "extent" );
        Map<String, Object> idFromCollection = jsonPath.getMap( "extent" );
        assertEquals( idFromCollection, idFromCollections,
                      " extent from collection is not equal to the collections extent" );
    }

}