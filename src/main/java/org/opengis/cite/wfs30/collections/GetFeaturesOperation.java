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
import static org.opengis.cite.wfs30.util.JsonUtils.parseExtent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.wfs30.CommonFixture;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.opengis.cite.wfs30.openapi3.OpenApiUtils;
import org.opengis.cite.wfs30.openapi3.TestPoint;
import org.opengis.cite.wfs30.util.BBox;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.model3.Operation;
import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.Path;
import com.reprezen.kaizen.oasparser.model3.Schema;

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

    @DataProvider(name = "collectionItemUrisWithLimit")
    public Object[][] collectionItemUrisWithLimits( ITestContext testContext ) {
        // TODO: find values between min and max described in OpenApi document (per collection)
        Object[][] collectionsData = new Object[collections.size() * 2][];
        int i = 0;
        for ( Map<String, Object> collection : collections ) {
            collectionsData[i++] = new Object[] { collection, 5 };
            collectionsData[i++] = new Object[] { collection, 15 };
        }
        return collectionsData;
    }

    @DataProvider(name = "collectionItemUrisWithBboxes")
    public Iterator<Object[]> collectionItemUrisWithBboxes( ITestContext testContext ) {
        List<Object[]> collectionsWithBboxes = new ArrayList<>();
        int i = 0;
        for ( Map<String, Object> collection : collections ) {
            BBox extent = parseExtent( collection );
            if ( extent != null ) {
                collectionsWithBboxes.add( new Object[] { collection, extent } );
                // These should include test cases which cross the
                // meridian,
                collectionsWithBboxes.add( new Object[] { collection, new BBox( -1.5, 50.0, 1.5, 53.0 ) } );
                // equator,
                collectionsWithBboxes.add( new Object[] { collection, new BBox( -80.0, -5.0, -70.0, 5.0 ) } );
                // 180 longitude,
                collectionsWithBboxes.add( new Object[] { collection, new BBox( 177.0, 65.0, -177.0, 70.0 ) } );
                // and polar regions.
                collectionsWithBboxes.add( new Object[] { collection, new BBox( -70.0, -20.0, -70.0, 160.0 ) } );
                collectionsWithBboxes.add( new Object[] { collection, new BBox( 70.0, -20.0, 70.0, 160.0 ) } );
            }
        }
        return collectionsWithBboxes.iterator();
    }

    @DataProvider(name = "collectionItemUrisWithTimes")
    public Object[][] collectionItemUrisWithTimes( ITestContext testContext ) {
        // TODO: find values
        Object[][] collectionsData = new Object[collections.size() * 3][];
        int i = 0;
        for ( Map<String, Object> collection : collections ) {
            // Example 6. A date-time
            collectionsData[i++] = new Object[] { collection, "2018-02-12T23%3A20%3A50Z" };
            // Example 7. A period using a start and end time
            collectionsData[i++] = new Object[] { collection, "2018-02-12T00%3A00%3A00Z%2F2018-03-18T12%3A31%3A12Z" };
            // Example 8. A period using start time and a duration
            collectionsData[i++] = new Object[] { collection, "2018-02-12T00%3A00%3A00Z%2FP1M6DT12H31M12S" };
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
    @Test(description = "Implements A.4.4.9. Validate the Get Features Operation (Requirement 17, 24)", groups = "getFeaturesBase", dataProvider = "collectionItemUris", dependsOnGroups = "collections")
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
    @Test(description = "Implements A.4.4.10. Validate the Get Features Operation Response (Requirement 25, 26)", groups = "getFeaturesBase", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
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
    @Test(description = "Implements A.4.4.10. Validate the Get Features Operation Response (Requirement 27)", groups = "getFeaturesBase", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateGetFeaturesOperationResponse_property_timeStamp( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        ResponseData response = collectionNameAndResponse.get( collectionName );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with name " + collectionName );

        JsonPath jsonPath = response.jsonPath();

        assertTimeStamp( collectionName, jsonPath, response.timeStampBeforeResponse, response.timeStampAfterResponse,
                         true );
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
    @Test(description = "Implements A.4.4.10. Validate the Get Features Operation Response (Requirement 29)", groups = "getFeaturesBase", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateGetFeaturesOperationResponse_property_numberReturned( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        ResponseData response = collectionNameAndResponse.get( collectionName );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with name " + collectionName );

        JsonPath jsonPath = response.jsonPath();

        assertNumberReturned( collectionName, jsonPath, true );
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
     *
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    @Test(description = "Implements A.4.4.10. Validate the Get Features Operation Response (Requirement 28)", groups = "getFeaturesBase", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateGetFeaturesOperationResponse_property_numberMatched( Map<String, Object> collection )
                            throws URISyntaxException {
        String collectionName = (String) collection.get( "name" );
        ResponseData response = collectionNameAndResponse.get( collectionName );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with name " + collectionName );

        JsonPath jsonPath = response.jsonPath();

        assertNumberMatched( collectionName, jsonPath, true );
    }

    /**
     * A.4.4.11. Limit Parameter (Test method 1)
     *
     * a) Test Purpose: Validate the proper handling of the limit parameter.
     *
     * b) Pre-conditions: Tests A.4.4.9 and A.4.4.10 have completed successfully.
     *
     * c) Test Method:
     *
     * Verify that the OpenAPI document correctly describes the limit parameter for the Get Features operation.
     *
     * d) References: Requirement 18
     *
     * Expected parameter:
     * 
     * <pre>
     * name: limit
     * in: query
     * required: false
     * schema:
     *   type: integer
     *   minimum: 1
     *   maximum: 10000 (example)
     *   default: 10 (example)
     * style: form
     * explode: false
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * 
     */
    @Test(description = "Implements A.4.4.11. Limit Parameter (Requirement 18)", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateLimitParameter( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        Parameter limit = findParameterByName( collectionName, "limit" );

        assertNotNull( limit, "Required limit parameter for collections item with name '" + collectionName
                              + "'  in OpenAPI document is missing" );

        String msg = "Expected property '%s' with value '%s' but was '%s'";

        assertEquals( limit.getName(), "limit", String.format( msg, "name", "limit", limit.getName() ) );
        assertEquals( limit.getIn(), "query", String.format( msg, "in", "query", limit.getIn() ) );
        assertFalse( limit.getRequired(), String.format( msg, "required", "false", limit.getRequired() ) );
        assertEquals( limit.getStyle(), "form", String.format( msg, "style", "form", limit.getStyle() ) );
        assertFalse( limit.getExplode(), String.format( msg, "explode", "false", limit.getExplode() ) );

        Schema schema = limit.getSchema();
        assertEquals( schema.getType(), "integer", String.format( msg, "schema -> type", "integer", schema.getType() ) );
        assertEquals( schema.getMinimum(), 1, String.format( msg, "schema -> minimum", "1", schema.getMinimum() ) );
        assertIntegerGreaterZero( schema.getMinimum(), "schema -> minimum" );
        assertIntegerGreaterZero( schema.getDefault(), "schema -> default" );
    }

    /**
     * A.4.4.11. Limit Parameter (Test method 2, 3)
     *
     * a) Test Purpose: Validate the proper handling of the limit parameter.
     *
     * b) Pre-conditions: Tests A.4.4.9 and A.4.4.10 have completed successfully.
     *
     * c) Test Method:
     *
     * Repeat Test A.4.4.9 using different values for the limit parameter.
     *
     * For each execution of Test A.4.4.9, repeat Test A.4.4.10 to validate the results.
     *
     * d) References: Requirement 19
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param limit
     *            limit parameter to request, never <code>null</code>
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    @Test(description = "Implements A.4.4.11. Limit Parameter (Requirement 19)", dataProvider = "collectionItemUrisWithLimit", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateLimitParameter_requests( Map<String, Object> collection, int limit )
                            throws URISyntaxException {
        String collectionName = (String) collection.get( "name" );

        String getFeaturesUrl = findGetFeatureUrlForGeoJson( collection );
        if ( getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with name " + collectionName
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        Date timeStampBeforeResponse = new Date();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).param( "limit", limit ).when().request( GET );
        response.then().statusCode( 200 );
        Date timeStampAfterResponse = new Date();

        JsonPath jsonPath = response.jsonPath();
        int numberOfFeatures = jsonPath.getList( "features" ).size();
        assertTrue( numberOfFeatures <= limit, "Number of features for collection with name " + collectionName
                                               + " is unexpected (was " + numberOfFeatures + "), expected are " + limit
                                               + " or less" );
        assertTimeStamp( collectionName, jsonPath, timeStampBeforeResponse, timeStampAfterResponse, false );
        assertNumberReturned( collectionName, jsonPath, false );
        assertNumberMatched( collectionName, jsonPath, false );
    }

    /**
     * A.4.4.12. Bounding Box Parameter (Test method 1)
     *
     * a) Test Purpose:Validate the proper handling of the bbox parameter.
     *
     * b) Pre-conditions: Tests A.4.4.9 and A.4.4.10 have completed successfully.
     *
     * c) Test Method:
     *
     * Verify that the OpenAPI document correctly describes the bbox parameter for the Get Features operation.
     *
     * d) References: Requirement 20
     *
     * Expected parameter:
     *
     * <pre>
     * name: bbox
     * in: query
     * required: false
     * schema:
     *   type: array
     *   minItems: 4
     *   maxItems: 6
     *   items:
     *     type: number
     * style: form
     * explode: false
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.12. Bounding Box (Requirement 20)", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateBboxParameter( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        Parameter bbox = findParameterByName( collectionName, "bbox" );

        assertNotNull( bbox, "Required bbox parameter for collections item with name '" + collectionName
                             + "'  in OpenAPI document is missing" );

        String msg = "Expected property '%s' with value '%s' but was '%s'";

        assertEquals( bbox.getName(), "bbox", String.format( msg, "name", "bbox", bbox.getName() ) );
        assertEquals( bbox.getIn(), "query", String.format( msg, "in", "query", bbox.getIn() ) );
        assertFalse( bbox.getRequired(), String.format( msg, "required", "false", bbox.getRequired() ) );
        assertEquals( bbox.getStyle(), "form", String.format( msg, "style", "form", bbox.getStyle() ) );
        assertFalse( bbox.getExplode(), String.format( msg, "explode", "false", bbox.getExplode() ) );

        Schema schema = bbox.getSchema();
        assertEquals( schema.getType(), "array", String.format( msg, "schema -> type", "array", schema.getType() ) );
        assertEquals( schema.getMinItems().intValue(), 4,
                      String.format( msg, "schema -> minItems", "4", schema.getMinItems() ) );
        assertEquals( schema.getMaxItems().intValue(), 6,
                      String.format( msg, "schema -> maxItems", "6", schema.getMaxItems() ) );

        String itemsType = schema.getItemsSchema().getType();
        assertEquals( itemsType, "number", String.format( msg, "schema -> items -> type", "number", itemsType ) );
    }

    /**
     * A.4.4.12. Bounding Box Parameter (Test method 1)
     *
     * a) Test Purpose:Validate the proper handling of the bbox parameter.
     *
     * b) Pre-conditions: Tests A.4.4.9 and A.4.4.10 have completed successfully.
     *
     * c) Test Method:
     *
     * Repeat Test A.4.4.9 using different values for the limit parameter.
     *
     * For each execution of Test A.4.4.9, repeat Test A.4.4.10 to validate the results.
     *
     * d) References: Requirement 21
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param bbox
     *            bbox parameter to request, never <code>null</code>
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    @Test(description = "Implements A.4.4.12. Bounding Box Parameter (Requirement 21)", dataProvider = "collectionItemUrisWithBboxes", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateBboxParameter_requests( Map<String, Object> collection, BBox bbox )
                            throws URISyntaxException {
        String collectionName = (String) collection.get( "name" );

        String getFeaturesUrl = findGetFeatureUrlForGeoJson( collection );
        if ( getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with name " + collectionName
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        Date timeStampBeforeResponse = new Date();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).param( "bbox",
                                                                                                bbox.asQueryParameter() ).when().request( GET );
        response.then().statusCode( 200 );
        Date timeStampAfterResponse = new Date();

        JsonPath jsonPath = response.jsonPath();
        assertTimeStamp( collectionName, jsonPath, timeStampBeforeResponse, timeStampAfterResponse, false );
        assertNumberReturned( collectionName, jsonPath, false );
        assertNumberMatched( collectionName, jsonPath, false );

        // TODO: assert returned features
    }

    /**
     * A.4.4.13. Time Parameter (Test method 1)
     *
     * a) Test Purpose: Validate the proper handling of the time parameter.
     *
     * b) Pre-conditions: Tests A.4.4.9 and A.4.4.10 have completed successfully.
     *
     * c) Test Method:
     *
     * Verify that the OpenAPI document correctly describes the time parameter for the Get Features operation.
     *
     * d) References: Requirement 22
     *
     * Expected parameter:
     *
     * <pre>
     * name: time
     * in: query
     * required: false
     * schema:
     *   type: string
     * style: form
     * explode: false
     * </pre>
     * 
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.13. Time (Requirement 22)", dataProvider = "collectionItemUris", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateTimeParameter( Map<String, Object> collection ) {
        String collectionName = (String) collection.get( "name" );
        Parameter time = findParameterByName( collectionName, "time" );

        assertNotNull( time, "Required time parameter for collections item with name '" + collectionName
                             + "'  in OpenAPI document is missing" );

        String msg = "Expected property '%s' with value '%s' but was '%s'";

        assertEquals( time.getName(), "time", String.format( msg, "name", "time", time.getName() ) );
        assertEquals( time.getIn(), "query", String.format( msg, "in", "query", time.getIn() ) );
        assertFalse( time.getRequired(), String.format( msg, "required", "false", time.getRequired() ) );
        assertEquals( time.getStyle(), "form", String.format( msg, "style", "form", time.getStyle() ) );
        assertFalse( time.getExplode(), String.format( msg, "explode", "false", time.getExplode() ) );

        Schema schema = time.getSchema();
        assertEquals( schema.getType(), "string", String.format( msg, "schema -> type", "string", schema.getType() ) );
    }

    /**
     * A.4.4.13. Time Parameter (Test method 2, 3)
     *
     * a) Test Purpose:Validate the proper handling of the bbox parameter.
     *
     * b) Pre-conditions: Tests A.4.4.9 and A.4.4.10 have completed successfully.
     *
     * c) Test Method:
     *
     * Repeat Test A.4.4.9 using different values for the limit parameter.
     *
     * For each execution of Test A.4.4.9, repeat Test A.4.4.10 to validate the results.
     *
     * d) References: Requirement 23
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param time
     *            time parameter to request, never <code>null</code>
     * @throws URISyntaxException
     *             if the creation of a uri fails
     *
     */
    @Test(description = "Implements A.4.4.12. Bounding Box Parameter (Requirement 23)", dataProvider = "collectionItemUrisWithTimes", dependsOnMethods = "validateGetFeaturesOperation")
    public void validateTimeParameter_requests( Map<String, Object> collection, String time )
                            throws URISyntaxException {
        String collectionName = (String) collection.get( "name" );

        String getFeaturesUrl = findGetFeatureUrlForGeoJson( collection );
        if ( getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with name " + collectionName
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        Date timeStampBeforeResponse = new Date();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).param( "time", time ).when().request( GET );
        response.then().statusCode( 200 );
        Date timeStampAfterResponse = new Date();

        JsonPath jsonPath = response.jsonPath();
        assertTimeStamp( collectionName, jsonPath, timeStampBeforeResponse, timeStampAfterResponse, false );
        assertNumberReturned( collectionName, jsonPath, false );
        assertNumberMatched( collectionName, jsonPath, false );

        // TODO: assert returned features
    }

    private void assertTimeStamp( String collectionName, JsonPath jsonPath, Date timeStampBeforeResponse,
                                  Date timeStampAfterResponse, boolean skipIfNoTimeStamp ) {
        String timeStamp = jsonPath.getString( "timeStamp" );
        if ( timeStamp == null )
            if ( skipIfNoTimeStamp )
                throw new SkipException( "Property timeStamp is not set in collection items '" + collectionName + "'" );
            else
                return;

        Date date = parseAsDate( timeStamp );
        assertTrue( date.before( timeStampAfterResponse ),
                    "timeStamp in response must be before the request was send ("
                                            + DATE_FORMAT.format( timeStampAfterResponse ) + ") but was '" + timeStamp
                                            + "'" );
        assertTrue( date.after( timeStampBeforeResponse ), "timeStamp in response must be after the request was send ("
                                                           + DATE_FORMAT.format( timeStampBeforeResponse )
                                                           + ") but was '" + timeStamp + "'" );
    }

    private void assertNumberReturned( String collectionName, JsonPath jsonPath, boolean skipIfNoNumberReturned ) {
        if ( !hasProperty( "numberReturned", jsonPath ) )
            if ( skipIfNoNumberReturned )
                throw new SkipException( "Property numberReturned is not set in collection items '" + collectionName
                                         + "'" );
            else
                return;

        int numberReturned = jsonPath.getInt( "numberReturned" );
        int numberOfFeatures = jsonPath.getList( "features" ).size();
        assertEquals( numberReturned, numberOfFeatures, "Value of numberReturned (" + numberReturned
                                                        + ") does not match the number of features in the response ("
                                                        + numberOfFeatures + ")" );
    }

    private void assertNumberMatched( String collectionName, JsonPath jsonPath, boolean skipIfNoNumberMatched )
                            throws URISyntaxException {
        if ( !hasProperty( "numberMatched", jsonPath ) )
            if ( skipIfNoNumberMatched )
                throw new SkipException( "Property numberMatched is not set in collection items '" + collectionName
                                         + "'" );
            else
                return;

        int numberMatched = jsonPath.getInt( "numberMatched" );
        int numberOfAllReturnedFeatures = collectNumberOfAllReturnedFeatures( jsonPath );
        assertEquals( numberMatched, numberOfAllReturnedFeatures,
                      "Value of numberReturned (" + numberMatched
                                              + ") does not match the number of features in all responses ("
                                              + numberOfAllReturnedFeatures + ")" );
    }

    private Parameter findParameterByName( String collectionName, String name ) {
        String collectionItemPath = "/" + COLLECTIONS.getPathItem() + "/" + collectionName + "/items";
        Path path = apiModel.getPath( collectionItemPath );
        if ( path != null ) {
            for ( Parameter parameter : path.getParameters() )
                if ( name.equals( parameter.getName() ) )
                    return parameter;
            Operation get = path.getOperation( "get" );
            for ( Parameter parameter : get.getParameters() )
                if ( name.equals( parameter.getName() ) )
                    return parameter;
        }
        return null;
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

    private void assertIntegerGreaterZero( Object value, String propertyName ) {
        if ( value instanceof Number )
            assertIntegerGreaterZero( ( (Number) value ).intValue(), propertyName );
        else if ( value instanceof String )
            try {
                int valueAsInt = Integer.parseInt( (String) value );
                assertIntegerGreaterZero( valueAsInt, propertyName );
            } catch ( NumberFormatException e ) {
                String msg = "Expected property '%s' to be an integer, but was '%s'";
                throw new AssertionError( String.format( msg, propertyName, value ) );
            }
    }

    private void assertIntegerGreaterZero( int value, String propertyName ) {
        String msg = "Expected property '%s' to be an integer greater than 0, but was '%s'";
        assertTrue( value > 0, String.format( msg, propertyName, value ) );
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
