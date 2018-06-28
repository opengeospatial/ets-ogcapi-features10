package org.opengis.cite.wfs30.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.wfs30.SuiteAttribute.API_MODEL;
import static org.opengis.cite.wfs30.WFS3.GEOJSON_MIME_TYPE;
import static org.opengis.cite.wfs30.WFS3.PATH.COLLECTIONS;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinkToItself;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.wfs30.util.JsonUtils.findUnsupportedTypes;
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
public class GetFeaturesOperation extends CommonFixture {

    private final Map<String, Response> collectionNameAndResponse = new HashMap<>();

    private List<Map<String, Object>> collections;

    private OpenApi3 apiModel;

    @DataProvider(name = "collectionItemUris")
    public Object[][] collectionItemUris( ITestContext testContext ) {
        Object[][] collectionsData = new Object[collections.size()][];
        for ( int i = 0; i < collections.size(); i++ ) {
            collectionsData[i] = new Object[] { collections.get( i ) };
        }
        return collectionsData;
    }

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
        this.collections = (List<Map<String, Object>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS.getName() );
    }

    /**
     * A.4.4.9. Validate the Get Features Operation
     *
     * a) Test Purpose: Validate that the Get Features Operation behaves as required.
     *
     * b) Pre-conditions:
     *
     * A feature collection name is provided by test A.4.4.6
     *
     * Path = /collections/{name}/items
     *
     * c) Test Method:
     *
     * DO FOR each /collections{name}/items test point
     *
     * Issue an HTTP GET request using the test point URI
     *
     * Go to test A.4.4.10
     *
     * d) References: Requirement 17
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.9. Validate the Get Features Operation (Requirement 17, 24)", dataProvider = "collectionItemUris", dependsOnGroups = "collections")
    public void validateGetFeaturesOperation( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );

        String getFeaturesUrl = findGetFeatureUrlForGeoJson( collection );
        if ( getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with name " + collectionName
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );

        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );
        collectionNameAndResponse.put( collectionName, response );
    }

    /**
     * A.4.4.10. Validate the Get Features Operation Response (Part 1)
     *
     * a) Test Purpose: Validate the Get Feature Operation Response.
     *
     * b) Pre-conditions: A collection of Features has been retrieved
     *
     * c) Test Method:
     *
     * Validate that the following links are included in the response document: To itself, Alternate encodings of this
     * document in every other media type as identified by the compliance classes for this server.
     *
     * Validate that each link includes a rel and type parameter.
     *
     * d) References: Requirements 25, 26
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.10. Validate the Get Features Operation Response (Requirement 25, 26)", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateGetFeaturesOperationResponse_Links( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        Response response = collectionNameAndResponse.get( collectionName );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with name " + collectionName );

        List<TestPoint> testPointsForNamedCollection = OpenApiUtils.retrieveTestPoints( apiModel, COLLECTIONS,
                                                                                        collectionName + "/items" );
        if ( testPointsForNamedCollection.isEmpty() )
            throw new SkipException( "Could not find collection with name " + collectionName
                                     + " in the OpenAPI document" );
        TestPoint testPoint = testPointsForNamedCollection.get( 0 );

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> links = jsonPath.getList( "links" );

        // Validate that the retrieved document includes links for: Itself
        Map<String, Object> linkToSelf = findLinkToItself( links );
        assertNotNull( linkToSelf, "Feature Collection Metadata document must include a link for itself" );

        // Validate that the retrieved document includes links for: Alternate encodings of this document in
        // every other media type as identified by the compliance classes for this server.
        List<String> mediaTypesToSupport = createListOfMediaTypesToSupport( testPoint, linkToSelf );
        List<Map<String, Object>> alternateLinks = findLinksWithSupportedMediaTypeByRel( links, mediaTypesToSupport,
                                                                                         "alternate" );
        List<String> typesWithoutLink = findUnsupportedTypes( alternateLinks, mediaTypesToSupport );
        assertTrue( typesWithoutLink.isEmpty(),
                    "Feature Collection Metadata document must include links for alternate encodings. Missing links for types "
                                            + typesWithoutLink );

        // Validate that each link includes a rel and type parameter.
        List<String> linksWithoutRelOrType = findLinksWithoutRelOrType( links );
        assertTrue( linksWithoutRelOrType.isEmpty(),
                    "Links for alternate encodings must include a rel and type parameter. Missing for links "
                                            + linksWithoutRelOrType );
    }

    /**
     * A.4.4.10. Validate the Get Features Operation Response (Part 2)
     *
     * a) Test Purpose: Validate the Get Feature Operation Response.
     *
     * b) Pre-conditions: A collection of Features has been retrieved
     *
     * c) Test Method:
     * 
     * If a property timeStamp is included in the response, validate that it is close to the current time.
     *
     * If a property numberReturned is included in the response, validate that the number is equal to the number of
     * features in the response.
     *
     * If a property numberMatched is included in the response, iteratively follow the next links until no next link is
     * included and count the aggregated number of features returned in all responses during the iteration. Validate
     * that the value is identical to the numberReturned stated in the initial response.
     *
     * d) References: Requirements 27, 28 and 29
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.10. Validate the Get Features Operation Response (Requirement 27, 28, 29)", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateGetFeaturesOperationResponse_Properties( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        Response response = collectionNameAndResponse.get( collectionName );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with name " + collectionName );

        // TODO
    }

    private String findGetFeatureUrlForGeoJson( Map<String, Object> collection ) {
        List<Object> links = (List<Object>) collection.get( "links" );
        for ( Object linkObject : links ) {
            Map<String, Object> link = (Map<String, Object>) linkObject;
            Object rel = link.get( "rel" );
            Object type = link.get( "type" );
            if ( "item".equals( rel ) && GEOJSON_MIME_TYPE.equals( type ) )
                return (String) link.get( "href" );
        }
        return null;
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
