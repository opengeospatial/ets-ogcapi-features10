package org.opengis.cite.wfs30.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.wfs30.SuiteAttribute.API_MODEL;
import static org.opengis.cite.wfs30.WFS3.GEOJSON_MIME_TYPE;
import static org.opengis.cite.wfs30.WFS3.PATH.COLLECTIONS;
import static org.opengis.cite.wfs30.util.JsonUtils.collectNumberOfAllReturnedFeatures;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinkByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.wfs30.util.JsonUtils.findUnsupportedTypes;
import static org.opengis.cite.wfs30.util.JsonUtils.hasProperty;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetFeaturesOperation extends CommonFixture {

    private static final ISO8601DateFormat DATE_FORMAT = new ISO8601DateFormat();

    private final Map<String, ResponseData> collectionNameAndResponse = new HashMap<>();

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

        Date timeStampBeforeResponse = new Date();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );
        Date timeStampAfterResponse = new Date();
        ResponseData responseData = new ResponseData( response, timeStampBeforeResponse, timeStampAfterResponse );
        collectionNameAndResponse.put( collectionName, responseData );
    }

    /**
     * A.4.4.10. Validate the Get Features Operation Response (Test method 2, 3)
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
        ResponseData response = collectionNameAndResponse.get( collectionName );
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
        Map<String, Object> linkToSelf = findLinkByRel( links, "self" );
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
     * A.4.4.10. Validate the Get Features Operation Response (Test method 4)
     *
     * a) Test Purpose: Validate the Get Feature Operation Response.
     *
     * b) Pre-conditions: A collection of Features has been retrieved
     *
     * c) Test Method:
     *
     * If a property timeStamp is included in the response, validate that it is close to the current time.
     *
     * d) References: Requirement 27
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.10. Validate the Get Features Operation Response (Requirement 27)", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateGetFeaturesOperationResponse_property_timeStamp( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        ResponseData response = collectionNameAndResponse.get( collectionName );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with name " + collectionName );

        JsonPath jsonPath = response.jsonPath();

        String timeStamp = jsonPath.getString( "timeStamp" );
        if ( timeStamp == null )
            throw new SkipException( "Property timeStamp is not set in collection items '" + collectionName + "'" );

        Date date = parseAsDate( timeStamp );
        assertTrue( date.before( response.timeStampAfterResponse ),
                    "timeStamp in response must be before the request was send ("
                                            + DATE_FORMAT.format( response.timeStampAfterResponse ) + ") but was '"
                                            + timeStamp + "'" );
        assertTrue( date.after( response.timeStampBeforeResponse ),
                    "timeStamp in response must be after the request was send ("
                                            + DATE_FORMAT.format( response.timeStampBeforeResponse ) + ") but was '"
                                            + timeStamp + "'" );
    }

    /**
     * A.4.4.10. Validate the Get Features Operation Response (Test method 5)
     *
     * a) Test Purpose: Validate the Get Feature Operation Response.
     *
     * b) Pre-conditions: A collection of Features has been retrieved
     *
     * c) Test Method:
     *
     * If a property numberReturned is included in the response, validate that the number is equal to the number of
     * features in the response.
     *
     * d) References: Requirement 29
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.10. Validate the Get Features Operation Response (Requirement 29)", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateGetFeaturesOperationResponse_property_numberReturned( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        ResponseData response = collectionNameAndResponse.get( collectionName );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with name " + collectionName );

        JsonPath jsonPath = response.jsonPath();

        if ( !hasProperty( "numberReturned", jsonPath ) ) {
            throw new SkipException( "Property numberReturned is not set in collection items '" + collectionName + "'" );
        }
        int numberReturned = jsonPath.getInt( "numberReturned" );

        int numberOfFeatures = jsonPath.getList( "features" ).size();
        assertEquals( numberReturned, numberOfFeatures, "Value of numberReturned (" + numberReturned
                                                        + ") does not match the number of features in the response ("
                                                        + numberOfFeatures + ")" );
    }

    /**
     * A.4.4.10. Validate the Get Features Operation Response (Test method 6)
     *
     * a) Test Purpose: Validate the Get Feature Operation Response.
     *
     * b) Pre-conditions: A collection of Features has been retrieved
     *
     * c) Test Method:
     *
     * If a property numberMatched is included in the response, iteratively follow the next links until no next link is
     * included and count the aggregated number of features returned in all responses during the iteration. Validate
     * that the value is identical to the numberReturned stated in the initial response.
     *
     * d) References: Requirement 28
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    @Test(description = "Implements A.4.4.10. Validate the Get Features Operation Response (Requirement 28)", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateGetFeaturesOperationResponse_property_numberMatched( Map<String, Object> collection )
                            throws URISyntaxException {
        String collectionName = (String) collection.get( "name" );
        ResponseData response = collectionNameAndResponse.get( collectionName );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with name " + collectionName );

        JsonPath jsonPath = response.jsonPath();

        if ( !hasProperty( "numberMatched", jsonPath ) ) {
            throw new SkipException( "Property numberMatched is not set in collection items '" + collectionName + "'" );
        }
        int numberMatched = jsonPath.getInt( "numberMatched" );
        int numberOfAllReturnedFeatures = collectNumberOfAllReturnedFeatures( jsonPath );
        assertEquals( numberMatched, numberOfAllReturnedFeatures,
                      "Value of numberReturned (" + numberMatched
                                              + ") does not match the number of features in all responses ("
                                              + numberOfAllReturnedFeatures + ")" );
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

    private Date parseAsDate( String timeStamp ) {

        try {
            return DATE_FORMAT.parse( timeStamp );
        } catch ( ParseException e ) {
            throw new AssertionError( "timeStamp " + timeStamp + "is not a valid date" );
        }
    }

    private class ResponseData {

        private final Response response;

        private final Date timeStampBeforeResponse;

        private final Date timeStampAfterResponse;

        public ResponseData( Response response, Date timeStampBeforeResponse, Date timeStampAfterResponse ) {
            this.response = response;
            this.timeStampBeforeResponse = timeStampBeforeResponse;
            this.timeStampAfterResponse = timeStampAfterResponse;
        }

        public JsonPath jsonPath() {
            return response.jsonPath();
        }
    }
}
