package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.IUT;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollectionsMetadata;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinkByRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findUnsupportedTypes;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.linkIncludesRelAndType;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseAsListOfMaps;
import static org.testng.Assert.assertNotNull;

import java.net.URI;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
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
 * A.2.5. Feature Collections {root}/collections
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureCollections extends CommonDataFixture {

    private final Map<TestPoint, Response> testPointAndResponses = new HashMap<>();

    private final Map<TestPoint, List<Map<String, Object>>> testPointAndCollections = new HashMap<>();

    private Object[][] testPointsData;

    @DataProvider(name = "collectionsUris")
    public Object[][] collectionsUris( ITestContext testContext ) {
        if ( this.testPointsData == null ) {
            URI iut = (URI) testContext.getSuite().getAttribute( IUT.getName() );
            List<TestPoint> testPoints = retrieveTestPointsForCollectionsMetadata( getApiModel(), iut );
            this.testPointsData = new Object[testPoints.size()][];
            int i = 0;
            for ( TestPoint testPoint : testPoints ) {
                this.testPointsData[i++] = new Object[] { testPoint };
            }
        }
        return testPointsData;
    }

    @AfterClass
    public void storeCollectionsInTestContext( ITestContext testContext ) {
        List<Map<String, Object>> collections = new ArrayList<>();
        for ( List<Map<String, Object>> testPointAndCollection : testPointAndCollections.values() ) {
            collections.addAll( testPointAndCollection );
        }
        testContext.getSuite().setAttribute( SuiteAttribute.COLLECTIONS.getName(), collections );
    }

    @AfterClass
    public void storeCollectionsResponseInTestContext( ITestContext testContext ) {
        Map<TestPoint, JsonPath> collectionsResponses = new HashMap<>();
        for ( Map.Entry<TestPoint, Response> testPointAndResponse : testPointAndResponses.entrySet() ) {
            if ( testPointAndResponse.getValue() != null ) {
                JsonPath jsonPath = testPointAndResponse.getValue().jsonPath();
                collectionsResponses.put( testPointAndResponse.getKey(), jsonPath );
            }
        }
        testContext.getSuite().setAttribute( SuiteAttribute.COLLECTIONS_RESPONSE.getName(), collectionsResponses );
    }
    
    /**
     * <pre>
     * Abstract Test 9: /ats/core/fc-md-op
     * Test Purpose:  Validate that information about the Collections can be retrieved from the expected location.
     * Requirement: /req/core/fc-md-op
     *
     * Test Method
     *  1. Issue an HTTP GET request to the URL {root}/collections
     *  2. Validate that a document was returned with a status code 200
     *  3. Validate the contents of the returned document using test /ats/core/fc-md-success.
     * </pre>
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "Implements A.2.5. Feature Collections {root}/collections, Abstract Test 9 (Requirement /req/core/fc-md-op)", groups = "collections", dataProvider = "collectionsUris", dependsOnGroups = { "apidefinition",
                                                                                                                                                                                                                    "conformance" })
    public void validateFeatureCollectionsMetadataOperation( TestPoint testPoint ) {
        String testPointUri = new UriBuilder( testPoint ).buildUrl();
        Response response = init().baseUri( testPointUri ).accept( JSON ).when().request( GET );
        response.then().statusCode( 200 );
        this.testPointAndResponses.put( testPoint, response );
    }

    /**
     * Abstract Test 10, Test Method 1
     *
     * <pre>
     * Abstract Test 10: /ats/core/fc-md-success
     * Test Purpose: Validate that the Collections content complies with the required structure and contents.
     * Requirement: /req/core/fc-md-success, /req/core/crs84
     *
     * Test Method
     *  1. Validate that all response documents comply with /ats/core/fc-md-links
     * </pre>
     * 
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 10, Test Method 1 (Requirement /req/core/fc-md-success, /req/core/crs84)", groups = "collections", dataProvider = "collectionsUris", dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
    public void validateFeatureCollectionsMetadataOperationResponse_Links( TestPoint testPoint ) {
        Response response = testPointAndResponses.get( testPoint );
        if ( response == null )
            throw new SkipException( "Could not find a response for test point " + testPoint );

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> links = parseAsListOfMaps( "links", jsonPath );

        // Requirement 13 A (1): a link to this response document (relation: self)
        Map<String, Object> linkToSelf = findLinkByRel( links, "self" );
        assertNotNull( linkToSelf, "Feature Collections Metadata document must include a link for itself" );
        // Requirement 13B: All links SHALL include the rel and type link parameters.
        assertTrue( linkIncludesRelAndType( linkToSelf ), "Link to itself must include a rel and type parameter" );

        // Requirement 13 A (2): a link to the response document in every other media type supported by the server
        // (relation: alternate)
        // Dev: Supported media type are identified by the compliance classes for this server
        List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForOtherResources( linkToSelf );
        List<Map<String, Object>> alternateLinks = findLinksWithSupportedMediaTypeByRel( links, mediaTypesToSupport,
                                                                                         "alternate" );
        List<String> typesWithoutLink = findUnsupportedTypes( alternateLinks, mediaTypesToSupport );
        assertTrue( typesWithoutLink.isEmpty(),
                    "Feature Collections Metadata document must include links for alternate encodings. Missing links for types "
                                                + typesWithoutLink );

        // Requirement 13 B: All "self"/"alternate" links SHALL include the rel and type link parameters.
        Set<String> rels = new HashSet<>();
        rels.add("self");
        rels.add("alternate");
        List<String> linksWithoutRelOrType = findLinksWithoutRelOrType( alternateLinks, rels );
        assertTrue( linksWithoutRelOrType.isEmpty(),
                    "Links for alternate encodings must include a rel and type parameter. Missing for links "
                                                     + linksWithoutRelOrType );
    }

    /**
     * Abstract Test 10, Test Method 2
     *
     * <pre>
     * Abstract Test 10: /ats/core/fc-md-success
     * Test Purpose: Validate that the Collections content complies with the required structure and contents.
     * Requirement: /req/core/fc-md-success, /req/core/crs84
     *
     * Test Method
     *  2. Validate that all response documents comply with /ats/core/fc-md-items
     * </pre>
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 10, Test Method 2 (Requirement /req/core/fc-md-success, /req/core/crs84)", groups = "collections", dataProvider = "collectionsUris", dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
    public void validateFeatureCollectionsMetadataOperationResponse_Items( TestPoint testPoint ) {
        Response response = testPointAndResponses.get( testPoint );
        if ( response == null )
            throw new SkipException( "Could not find a response for test point " + testPoint );
        JsonPath jsonPath = response.jsonPath();
        List<Object> collections = jsonPath.getList( "collections" );

        // Test method cannot be verified as the provided collections are not known.

        this.testPointAndCollections.put( testPoint, createCollectionsMap( collections ) );
    }

    /**
     * Abstract Test 10, Test Method 3
     *
     * <pre>
     * Abstract Test 10: /ats/core/fc-md-success
     * Test Purpose: Validate that the Collections content complies with the required structure and contents.
     * Requirement: /req/core/fc-md-success, /req/core/crs84
     *
     * Test Method
     *  3. In case the response includes a "crs" property, validate that the first value is either "http://www.opengis.net/def/crs/OGC/1.3/CRS84" or "http://www.opengis.net/def/crs/OGC/0/CRS84h"
     * </pre>
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 10, Test Method 3 (Requirement /req/core/fc-md-success, /req/core/crs84)", groups = "collections", dataProvider = "collectionsUris", dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
    public void validateFeatureCollectionsMetadataOperationResponse_CrsProperty( TestPoint testPoint ) {
        Response response = testPointAndResponses.get( testPoint );
        if ( response == null )
            throw new SkipException( "Could not find a response for test point " + testPoint );

        // Dev: Requirement?
    }

    /**
     * Abstract Test 10, Test Method 4
     *
     * <pre>
     * Abstract Test 10: /ats/core/fc-md-success
     * Test Purpose: Validate that the Collections content complies with the required structure and contents.
     * Requirement: /req/core/fc-md-success, /req/core/crs84
     *
     * Test Method
     *  4. Validate the collections content for all supported media types using the resources and tests identified in Schema and Tests for Collections content
     * </pre>
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 10, Test Method 4 (Requirement /req/core/fc-md-success, /req/core/crs84)", groups = "collections", dataProvider = "collectionsUris", dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
    public void validateFeatureCollectionsMetadataOperationResponse_Content( TestPoint testPoint ) {
        Response response = testPointAndResponses.get( testPoint );
        if ( response == null )
            throw new SkipException( "Could not find a response for test point " + testPoint );

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> links = parseAsListOfMaps( "links", jsonPath );
        boolean linksAreAvailable = links != null && !links.isEmpty();
        assertTrue( linksAreAvailable, "Feature Collections Metadata document does not contain links." );

        List<Map<String, Object>> collections = parseAsListOfMaps( "collections", jsonPath );
        boolean collectionssAreAvailable = collections != null && !collections.isEmpty();
        assertTrue( collectionssAreAvailable, "Feature Collections Metadata document does not contain collections." );
    }

    private List<Map<String, Object>> createCollectionsMap( List<Object> collections ) {
        List<Map<String, Object>> collectionsMap = new ArrayList<>();
        for (Object collectionObj : collections) {
            Map<String, Object> collection = (Map<String, Object>) collectionObj;
            if (null != collection.get("id")) {
                String itemType = (String) collection.get("itemType");
                if (StringUtils.isEmpty(itemType) || itemType.equalsIgnoreCase("feature")) {
                    List<Object> links = (List<Object>) collection.get("links");
                    for (Object linkObj : links) {
                        Map<String, Object> link = (Map<String, Object>) linkObj;
                        if (link.get("rel").equals("items")) {
                            collectionsMap.add(collection);
                            break;
                        }
                    }
                }
                if (noOfCollections > 0 && collectionsMap.size() >= noOfCollections) {
                    return collectionsMap;
                }
            }
        }
        return collectionsMap;
    }

}