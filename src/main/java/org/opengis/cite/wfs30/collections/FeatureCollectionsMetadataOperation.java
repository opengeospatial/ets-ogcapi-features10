package org.opengis.cite.wfs30.collections;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.wfs30.SuiteAttribute.API_MODEL;
import static org.opengis.cite.wfs30.WFS3.PATH.COLLECTIONS;
import static org.opengis.cite.wfs30.openapi3.OpenApiUtils.retrieveTestPoints;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinkByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.wfs30.util.JsonUtils.findUnsupportedTypes;
import static org.opengis.cite.wfs30.util.JsonUtils.linkIncludesRelAndType;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.cite.wfs30.CommonFixture;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.opengis.cite.wfs30.openapi3.OpenApiUtils;
import org.opengis.cite.wfs30.openapi3.TestPoint;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureCollectionsMetadataOperation extends CommonFixture {

    private final Map<TestPoint, Response> testPointAndResponses = new HashMap<>();

    private final Map<TestPoint, List<Map<String, Object>>> testPointAndCollections = new HashMap<>();

    private final List<String> collectionNamesFromLandingPage = new ArrayList<>();

    private OpenApi3 apiModel;

    private Object[][] testPointsData;

    @DataProvider(name = "collectionsUris")
    public Object[][] collectionsUris( ITestContext testContext ) {
        if ( this.testPointsData == null ) {
            OpenApi3 apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
            List<TestPoint> testPoints = retrieveTestPoints( apiModel, COLLECTIONS );
            this.testPointsData = new Object[][] { testPoints.toArray() };
        }
        return testPointsData;
    }

    @DataProvider(name = "collections")
    public Object[][] collections( ITestContext testContext ) {
        int length = 0;
        for ( List<Map<String, Object>> collections : testPointAndCollections.values() )
            length += collections.size();
        Object[][] objects = new Object[length][];
        int i = 0;
        for ( Map.Entry<TestPoint, List<Map<String, Object>>> testPointAndCollection : testPointAndCollections.entrySet() ) {
            for ( Map<String, Object> collection : testPointAndCollection.getValue() )
                objects[i++] = new Object[] { testPointAndCollection.getKey(), collection };
        }
        return objects;
    }

    @BeforeClass
    public void parseRequiredMetadata( ITestContext testContext ) {
        Response request = init().baseUri( rootUri.toString() ).accept( JSON ).when().request( GET, "/" );
        JsonPath jsonPath = request.jsonPath();

        List<Object> collections = jsonPath.getList( "collections" );
        for ( Object collectionObject : collections ) {
            Map<String, Object> collection = (Map<String, Object>) collectionObject;
            String collectionName = (String) collection.get( "name" );
            this.collectionNamesFromLandingPage.add( collectionName );
        }
    }

    @BeforeClass
    public void openApiDocument( ITestContext testContext ) {
        this.apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
    }

    @AfterClass
    public void storeCollectionsInTestContext( ITestContext testContext ) {
        List<Map<String, Object>> collections = new ArrayList<>();
        for ( List<Map<String, Object>> testPointAndCollection : testPointAndCollections.values() ) {
            collections.addAll( testPointAndCollection );
        }
        testContext.getSuite().setAttribute( SuiteAttribute.COLLECTIONS.getName(), collections );
    }

    /**
     * A.4.4.4. Validate the Feature Collections Metadata Operation
     *
     * a) Test Purpose: Validate that the Feature Collections Metadata Operation behaves as required
     *
     * b) Pre-conditions: Path = /collections
     *
     * c) Test Method:
     *
     * DO FOR each /collections test point - Issue an HTTP GET request using the test point URI
     *
     * Go to test A.4.4.5
     *
     * d) References: Requirement 9
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.4. Validate the Feature Collections Metadata Operation (Requirement 9, 10)", groups = "collections", dataProvider = "collectionsUris", dependsOnGroups = "apidefinition")
    public void validateFeatureCollectionsMetadataOperation( TestPoint testPoint ) {
        String testPointUri = testPoint.createUri();
        Response response = init().baseUri( testPointUri ).accept( JSON ).when().request( GET );
        response.then().statusCode( 200 );
        this.testPointAndResponses.put( testPoint, response );
    }

    /**
     * A.4.4.5. Validate the Feature Collections Metadata Operation Response (Part 1)
     *
     * a) Test Purpose: Validate that response to the Feature Collection Metadata Operation.
     *
     * b) Pre-conditions: A Feature Collection Metadata document has been retrieved
     *
     * c) Test Method:
     *
     * Validate that the retrieved document includes links for: Itself, Alternate encodings of this document in every
     * other media type as identified by the compliance classes for this server.
     *
     * Validate that each link includes a rel and type parameter
     *
     * d) References: Requirement 11
     * 
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.5. Validate the Feature Collections Metadata Operation Response (Requirement 11)", groups = "collections", dataProvider = "collectionsUris", dependsOnMethods = "validateFeatureCollectionsMetadataOperation")
    public void validateFeatureCollectionsMetadataOperationResponse_Links( TestPoint testPoint ) {
        Response response = testPointAndResponses.get( testPoint );
        if ( response == null )
            throw new SkipException( "Could not find a response for test point " + testPoint );

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> links = jsonPath.getList( "links" );

        // Validate that the retrieved document includes links for: Itself,
        Map<String, Object> linkToSelf = findLinkByRel( links, "self" );
        assertNotNull( linkToSelf, "Feature Collection Metadata document must include a link for itself" );
        assertTrue( linkIncludesRelAndType( linkToSelf ), "Link to itself must include a rel and type parameter" );

        // Validate that the retrieved document includes links for: Itself, Alternate encodings of this document in
        // every other media type as identified by the compliance classes for this server.
        List<String> mediaTypesToSupport = createListOfMediaTypesToSupport( testPoint, linkToSelf );
        List<Map<String, Object>> alternateLinks = findLinksWithSupportedMediaTypeByRel( links, mediaTypesToSupport,
                                                                                         "alternate" );
        List<String> typesWithoutLink = findUnsupportedTypes( alternateLinks, mediaTypesToSupport );
        assertTrue( typesWithoutLink.isEmpty(),
                    "Feature Collection Metadata document must include links for alternate encodings. Missing links for types "
                                            + typesWithoutLink );

        List<String> linksWithoutRelOrType = findLinksWithoutRelOrType( alternateLinks );
        assertTrue( linksWithoutRelOrType.isEmpty(),
                    "Links for alternate encodings must include a rel and type parameter. Missing for links "
                                            + linksWithoutRelOrType );
    }

    /**
     * A.4.4.5. Validate the Feature Collections Metadata Operation Response (Part 1)
     *
     * a) Test Purpose: Validate that response to the Feature Collection Metadata Operation.
     *
     * b) Pre-conditions: A Feature Collection Metadata document has been retrieved
     *
     * c) Test Method:
     *
     * Validate that the returned document includes a collections property for each collection in the dataset.
     *
     * d) References: Requirement 12
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.5. Validate the Feature Collections Metadata Operation Response (Requirement 12)", groups = "collections", dataProvider = "collectionsUris", dependsOnMethods = "validateFeatureCollectionsMetadataOperation")
    public void validateFeatureCollectionsMetadataOperationResponse_Collections( TestPoint testPoint ) {
        Response response = testPointAndResponses.get( testPoint );
        if ( response == null )
            throw new SkipException( "Could not find a response for test point " + testPoint );
        JsonPath jsonPath = response.jsonPath();
        List<Object> collections = jsonPath.getList( "collections" );

        List<String> missingCollectionNames = findMissingCollectionNames( collections );
        assertTrue( missingCollectionNames.isEmpty(),
                    "Feature Collection Metadata document must include a collections property for each collection in the dataset. Missing collection properties "
                                            + missingCollectionNames );
        this.testPointAndCollections.put( testPoint, createCollectionsMap( collections ) );
    }

    /**
     * A.4.4.6. Validate a Collections Metadata document (Part 1)
     *
     * a) Test Purpose: Validate a Collections Metadata document.
     *
     * b) Pre-conditions: A Collection metadata document has been retrieved.
     *
     * c) Test Method:
     *
     * Validate the collection metadata against the collectionInfo.yaml schema
     *
     * Validate that the collection metadata document includes links for: Itself, Alternate encodings of this document
     * in every other media type as identified by the compliance classes for this server.
     *
     * Validate that each link includes a rel and type parameter
     *
     * d) References: Requirement 13
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     * @param collection
     *            the collection to test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.6. Validate a Collections Metadata document (Requirement 13)", groups = "collections", dataProvider = "collections", dependsOnMethods = "validateFeatureCollectionsMetadataOperationResponse_Collections")
    public void validateCollectionsMetadataDocument_Links( TestPoint testPoint, Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        List<TestPoint> testPointsForNamedCollection = OpenApiUtils.retrieveTestPoints( apiModel, COLLECTIONS,
                                                                                        collectionName );
        if ( testPointsForNamedCollection.isEmpty() )
            throw new SkipException( "Could not find collection with name " + collectionName
                                     + " in the OpenAPI document" );

        List<String> mediaTypesToSupport = createListOfMediaTypesToSupport( testPointsForNamedCollection.get( 0 ), null );
        List<Map<String, Object>> links = (List<Map<String, Object>>) collection.get( "links" );

        List<Map<String, Object>> alternateLinks = findLinksWithSupportedMediaTypeByRel( links, mediaTypesToSupport,
                                                                                         "item" );
        List<String> typesWithoutLink = findUnsupportedTypes( alternateLinks, mediaTypesToSupport );
        assertTrue( typesWithoutLink.isEmpty(),
                    "Collections Metadata document must include links with relation 'item' for each supported encodings. Missing links for types "
                                            + typesWithoutLink );
        List<String> linksWithoutRelOrType = findLinksWithoutRelOrType( alternateLinks );
        assertTrue( linksWithoutRelOrType.isEmpty(),
                    "Links with relation 'item' for encodings must include a rel and type parameter. Missing for links "
                                            + linksWithoutRelOrType );
    }

    /**
     * A.4.4.6. Validate a Collections Metadata document (Part 2)
     *
     * a) Test Purpose: Validate a Collections Metadata document.
     *
     * b) Pre-conditions: A Collection metadata document has been retrieved.
     *
     * c) Test Method:
     *
     * Validate the extent property if it is provided
     *
     * d) References: Requirement 14
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     * @param collection
     *            the collection to test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.6. Validate a Collections Metadata document (Requirement 14)", groups = "collections", dataProvider = "collections", dependsOnMethods = "validateFeatureCollectionsMetadataOperationResponse_Collections")
    public void validateCollectionsMetadataDocument_Extent( TestPoint testPoint, Map<String, Object> collection ) {
        // TODO: validate the extent property
    }

    /**
     * A.4.4.7. Validate the Feature Collection Metadata Operation and A.4.4.8. Validate the Feature Collection Metadata
     * Operation Response
     * 
     * @param testPoint
     *            the test point to test, never <code>null</code>
     * @param collection
     *            the collection to test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.7. Validate the Feature Collection Metadata Operation (Requirement 15) and A.4.4.8. Validate the Feature Collection Metadata Operation Response (Requirement 16)", groups = "collections", dataProvider = "collections", dependsOnMethods = "validateFeatureCollectionsMetadataOperationResponse_Collections")
    public void validateTheFeatureCollectionMetadataOperationAndResponse( TestPoint testPoint,
                                                                          Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        List<TestPoint> testPointsForNamedCollection = OpenApiUtils.retrieveTestPoints( apiModel, COLLECTIONS,
                                                                                        collectionName );
        if ( testPointsForNamedCollection.isEmpty() )
            throw new SkipException( "Could not find collection with name " + collectionName
                                     + " in the OpenAPI document" );

        Response response = validateTheFeatureCollectionMetadataOperationAndResponse( testPointsForNamedCollection.get( 0 ) );
        validateFeatureCollectionMetadataOperationResponse( response, collection );
    }

    /**
     * A.4.4.7. Validate the Feature Collection Metadata Operation
     *
     * a) Test Purpose: Validate that the Feature Collection Metadata Operation behaves as required
     *
     * b) Pre-conditions:
     *
     * A feature collection name is provided by test A.4.4.6
     *
     * Path = /collections/{name}
     *
     * c) Test Method:
     *
     * DO FOR each /collections{name} test point
     *
     * Issue an HTTP GET request using the test point URI
     *
     * Go to test A.4.4.8
     *
     * d) References: Requirement 15
     * 
     * @param testPoint
     *            to test, never <code>null</code>
     */
    private Response validateTheFeatureCollectionMetadataOperationAndResponse( TestPoint testPoint ) {
        String testPointUri = testPoint.createUri();
        Response response = init().baseUri( testPointUri ).accept( JSON ).when().request( GET );
        response.then().statusCode( 200 );
        return response;
    }

    /**
     * A.4.4.8. Validate the Feature Collection Metadata Operation Response
     * 
     * a) Test Purpose: Validate that response to the Feature Collection Metadata Operation. b) Pre-conditions: A
     * Feature Collection Metadata document has been retrieved
     *
     * c) Test Method:
     *
     * Validate the retrieved document against the collectionInfo.yaml schema.
     *
     * Validate that this is the same document as that processed in Test A.4.4.6
     *
     * d) References: Requirement 16
     *
     * @param response
     *            the response for 'collection/{name}', never <code>null</code>
     * @param collection
     *            the expected collection, never <code>null</code>
     */
    private void validateFeatureCollectionMetadataOperationResponse( Response response, Map<String, Object> collection ) {
        JsonPath jsonPath = response.jsonPath();
        assertEquals( collection, jsonPath.get() );
    }

    private List<String> findMissingCollectionNames( List<Object> collections ) {
        List<String> missingCollectionNames = new ArrayList<>();
        for ( String collectionNameFromLandingPage : this.collectionNamesFromLandingPage ) {
            Map<String, Object> collection = findCollectionByName( collectionNameFromLandingPage, collections );
            if ( collection == null )
                missingCollectionNames.add( collectionNameFromLandingPage );
        }
        return missingCollectionNames;
    }

    private Map<String, Object> findCollectionByName( String collectionNameFromLandingPage, List<Object> collections ) {
        for ( Object collectionObject : collections ) {
            Map<String, Object> collection = (Map<String, Object>) collectionObject;
            Object collectionName = collection.get( "name" );
            if ( collectionNameFromLandingPage.equals( collectionName ) )
                return collection;
        }
        return null;
    }

    private List<Map<String, Object>> createCollectionsMap( List<Object> collections ) {
        List<Map<String, Object>> collectionsMap = new ArrayList<>();
        for ( Object collection : collections )
            collectionsMap.add( (Map<String, Object>) collection );
        return collectionsMap;
    }

    private List<String> createListOfMediaTypesToSupport( TestPoint testPoint, Map<String, Object> linkToSelf ) {
        Map<String, MediaType> contentMediaTypes = testPoint.getContentMediaTypes();
        List<String> mediaTypesToSupport = new ArrayList<>();
        mediaTypesToSupport.addAll( contentMediaTypes.keySet() );
        if ( linkToSelf != null )
            mediaTypesToSupport.remove( linkToSelf.get( "type" ) );
        return mediaTypesToSupport;
    }

}