package org.opengis.cite.ogcapifeatures10.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertFalse;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.SuiteAttribute.API_MODEL;
import static org.opengis.cite.ogcapifeatures10.SuiteAttribute.IUT;
import static org.opengis.cite.ogcapifeatures10.collections.FeaturesAssertions.assertIntegerGreaterZero;
import static org.opengis.cite.ogcapifeatures10.collections.FeaturesAssertions.assertNumberMatched;
import static org.opengis.cite.ogcapifeatures10.collections.FeaturesAssertions.assertNumberReturned;
import static org.opengis.cite.ogcapifeatures10.collections.FeaturesAssertions.assertTimeStamp;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveParameterByName;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollection;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollections;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinkByRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findUnsupportedTypes;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.formatDate;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.formatDateRange;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.formatDateRangeWithDuration;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseFeatureId;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseSpatialExtent;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseTemporalExtent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.CommonDataFixture;
import org.opengis.cite.ogcapifeatures10.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.opengis.cite.ogcapifeatures10.util.BBox;
import org.opengis.cite.ogcapifeatures10.util.TemporalExtent;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.Schema;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * A.2.7. Features {root}/collections/{collectionId}/items
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Features extends CommonDataFixture {

    private final Map<String, ResponseData> collectionNameAndResponse = new HashMap<>();

    private List<Map<String, Object>> collections;

    private OpenApi3 apiModel;

    private URI iut;

    @DataProvider(name = "collectionItemUris")
    public Iterator<Object[]> collectionItemUris( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            collectionsData.add( new Object[] { collection } );
        }
        return collectionsData.iterator();
    }

    @DataProvider(name = "collectionPaths")
    public Iterator<Object[]> collectionPaths( ITestContext testContext ) {
        this.iut = (URI) testContext.getSuite().getAttribute( IUT.getName() );
        List<TestPoint> testPointsForCollections = retrieveTestPointsForCollections( apiModel, iut, noOfCollections );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( TestPoint testPointForCollections : testPointsForCollections ) {
            collectionsData.add( new Object[] { testPointForCollections } );
        }
        return collectionsData.iterator();
    }

    @DataProvider(name = "collectionItemUrisWithLimit")
    public Iterator<Object[]> collectionItemUrisWithLimits( ITestContext testContext ) {
        URI iut = (URI) testContext.getSuite().getAttribute( IUT.getName() );
        List<Object[]> collectionsWithLimits = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            String collectionId = (String) collection.get( "id" );
            List<TestPoint> testPoints = retrieveTestPointsForCollection( apiModel, iut, collectionId );
            for ( TestPoint testPoint : testPoints ) {
                Parameter limit = retrieveParameterByName( testPoint.getPath(), apiModel, "limit" );
                if ( limit != null && limit.getSchema() != null ) {
                    int min = limit.getSchema().getMinimum().intValue();
                    int max = limit.getSchema().getMaximum().intValue();
                    if ( min == max ) {
                        collectionsWithLimits.add( new Object[] { collection, min } );
                    } else {
                        collectionsWithLimits.add( new Object[] { collection, min } );
                        int betweenMinAndMax = min + ( ( max - min ) / 2 );
                        collectionsWithLimits.add( new Object[] { collection, betweenMinAndMax } );
                    }
                }
            }
        }
        return collectionsWithLimits.iterator();
    }

    @DataProvider(name = "collectionItemUrisWithBboxes")
    public Iterator<Object[]> collectionItemUrisWithBboxes( ITestContext testContext ) {
        List<Object[]> collectionsWithBboxes = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            BBox extent = parseSpatialExtent( collection );
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
                collectionsWithBboxes.add( new Object[] { collection, new BBox( -180.0, 85.0, 180.0, 90.0 ) } );
                collectionsWithBboxes.add( new Object[] { collection, new BBox( -180.0, -85.0, 180.0, -90.0 ) } );
            }
        }
        return collectionsWithBboxes.iterator();
    }

    @DataProvider(name = "collectionItemUrisWithTimes")
    public Iterator<Object[]> collectionItemUrisWithTimes( ITestContext testContext ) {
        List<Object[]> collectionsWithTimes = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            TemporalExtent temporalExtent = parseTemporalExtent( collection );
            if ( temporalExtent != null ) {
                ZonedDateTime begin = temporalExtent.getBegin();
                ZonedDateTime end = temporalExtent.getEnd();

                Duration between = Duration.between( begin, end );
                Duration quarter = between.dividedBy( 4 );
                ZonedDateTime beginInterval = begin.plus( quarter );
                ZonedDateTime endInterval = beginInterval.plus( quarter );

                // Example 6. A date-time
                collectionsWithTimes.add( new Object[] { collection, formatDate( begin ), beginInterval, null } );
                // Example 7. A period using a start and end time
                collectionsWithTimes.add( new Object[] { collection, formatDateRange( beginInterval, endInterval ),
                                                         beginInterval, endInterval } );
                // Example 8. A period using start time and a duration
                LocalDate beginIntervalDate = beginInterval.toLocalDate();
                LocalDate endIntervalDate = beginIntervalDate.plusDays( 2 );
                collectionsWithTimes.add( new Object[] { collection,
                                                         formatDateRangeWithDuration( beginIntervalDate,
                                                                                      endIntervalDate ),
                                                         beginIntervalDate, endIntervalDate } );
            }
        }
        return collectionsWithTimes.iterator();
    }

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
        this.collections = (List<Map<String, Object>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS.getName() );
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
     * </pre>
     *
     * @param testContext
     *            used to fill the FEATUREIDS, never <code>null</code>
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Abstract Test 13 (Requirement /req/core/fc-op)", groups = "getFeaturesBase", dataProvider = "collectionItemUris", dependsOnGroups = "collections", alwaysRun = true)
    public void validateFeaturesOperation( ITestContext testContext, Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );

        String featuresUrl = findFeaturesUrlForGeoJson( collection );
        if ( featuresUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );

        ZonedDateTime timeStampBeforeResponse = ZonedDateTime.now();
        Response response = init().baseUri( featuresUrl ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );
        ZonedDateTime timeStampAfterResponse = ZonedDateTime.now();
        ResponseData responseData = new ResponseData( response, timeStampBeforeResponse, timeStampAfterResponse );
        collectionNameAndResponse.put( collectionId, responseData );

        addFeatureIdToTestContext( testContext, collectionId, response );
    }

    /**
     * Abstract Test 22, Test Method 1
     *
     * <pre>
     * Abstract Test 22: /ats/core/fc-response
     * Test Purpose: Validate that the Feature Collections complies with the require structure and contents.
     * Requirement: /req/core/fc-response
     *
     * Test Method
     *   1. Validate that the type property is present and has a value of FeatureCollection
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Abstract Test 22, Test Method 1 (Requirement /req/core/fc-response)", dataProvider = "collectionItemUris", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void validateFeaturesOperationResponse_TypeProperty( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        ResponseData response = collectionNameAndResponse.get( collectionId );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        JsonPath jsonPath = response.jsonPath();
        String type = jsonPath.get( "type" );
        assertNotNull( type, "type property is missing" );
        assertEquals( type, "FeatureCollection", "Expected type property value of FeatureCollection but was " + type );
    }

    /**
     * Abstract Test 22, Test Method 2
     *
     * <pre>
     * Abstract Test 22: /ats/core/fc-response
     * Test Purpose: Validate that the Feature Collections complies with the require structure and contents.
     * Requirement: /req/core/fc-response
     *
     * Test Method
     *   2. Validate the features property is present and that it is populated with an array of feature items.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Abstract Test 22, Test Method 2 (Requirement /req/core/fc-response)", dataProvider = "collectionItemUris", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void validateFeaturesOperationResponse_FeaturesProperty( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        ResponseData response = collectionNameAndResponse.get( collectionId );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        JsonPath jsonPath = response.jsonPath();
        String type = jsonPath.get( "features" );
        assertNotNull( type, "features property is missing" );
    }

    /**
     * Abstract Test 22, Test Method 4 (Abstract Test 23)
     *
     * <pre>
     * Abstract Test 22: /ats/core/fc-response
     * Test Purpose: Validate that the Feature Collections complies with the require structure and contents.
     * Requirement: /req/core/fc-response
     *
     * Test Method
     *   4. If the links property is present, validate that all entries comply with /ats/core/fc-links
     * </pre>
     *
     * <pre>
     * Abstract Test 23: /ats/core/fc-links
     * Test Purpose: Validate that the required links are included in the Collections document.
     * Requirement: /req/core/fc-links, /req/core/fc-rel-type
     *
     * Test Method:
     * Verify that the response document includes:
     *   1. a link to this response document (relation: self),
     *   2. a link to the response document in every other media type supported by the server (relation: alternate).
     *
     * Verify that all links include the rel and type link parameters.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Abstract Test 22, Test Method 4 (Requirement /req/core/fc-response) - Abstract Test 23 (Requirement /req/core/fc-links, /req/core/fc-rel-type)", dataProvider = "collectionItemUris", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void validateFeaturesOperationResponse_Links( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        ResponseData response = collectionNameAndResponse.get( collectionId );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> links = jsonPath.getList( "links" );

        // 1. a link to this response document (relation: self)
        Map<String, Object> linkToSelf = findLinkByRel( links, "self" );
        assertNotNull( linkToSelf, "Feature Collection Metadata document must include a link for itself" );

        // 2. a link to the response document in every other media type supported by the server (relation: alternate)
        // Dev: Supported media type are identified by the compliance classes for this server
        List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForFeatureCollectionsAndFeatures( linkToSelf );
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
     * Abstract Test 22, Test Method 5 (Abstract Test 24)
     *
     * <pre>
     * Abstract Test 22: /ats/core/fc-response
     * Test Purpose: Validate that the Feature Collections complies with the require structure and contents.
     * Requirement: /req/core/fc-response
     *
     * Test Method
     *   5. If the timeStamp property is present, validate that it complies with /ats/core/fc-timeStamp
     * </pre>
     *
     * <pre>
     * Abstract Test 24: /ats/core/fc-timeStamp
     * Test Purpose: Validate the timeStamp parameter returned with a Features response
     * Requirement: /req/core/fc-timeStamp
     *
     * Test Method: Validate that the timeStamp value is set to the time when the response was generated.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Abstract Test 22, Test Method 5 (Requirement /req/core/fc-response) - Abstract Test 24 (Requirement /req/core/fc-timeStamp)", dataProvider = "collectionItemUris", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void validateFeaturesOperationResponse_TimeStamp( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        ResponseData response = collectionNameAndResponse.get( collectionId );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        JsonPath jsonPath = response.jsonPath();

        assertTimeStamp( collectionId, jsonPath, response.timeStampBeforeResponse, response.timeStampAfterResponse,
                         true );
    }

    /**
     * Abstract Test 22, Test Method 6 (Abstract Test 25)
     *
     * <pre>
     * Abstract Test 22: /ats/core/fc-response
     * Test Purpose: Validate that the Feature Collections complies with the require structure and contents.
     * Requirement: /req/core/fc-response
     *
     * Test Method
     *   6. If the numberMatched property is present, validate that it complies with /ats/core/fc-numberMatched
     * </pre>
     *
     * <pre>
     * Abstract Test 25: /ats/core/fc-numberMatched
     * Test Purpose: Validate the numberMatched parameter returned with a Features response
     * Requirement: /req/core/fc-numberMatched
     *
     * Test Method: Validate that the value of the numberMatched parameter is identical to the number of features in the feature collections that match the selection parameters like bbox, datetime or additional filter parameters.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     *
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Abstract Test 22, Test Method 6 (Requirement /req/core/fc-response) - Abstract Test 25 (Requirement /req/core/fc-numberMatched)", dataProvider = "collectionItemUris", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void validateFeaturesOperationResponse_property_numberMatched( Map<String, Object> collection )
                            throws URISyntaxException {
        String collectionId = (String) collection.get( "id" );
        ResponseData response = collectionNameAndResponse.get( collectionId );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        JsonPath jsonPath = response.jsonPath();

        assertNumberMatched( apiModel, iut, collectionId, jsonPath, true );
    }

    /**
     * Abstract Test 22, Test Method 7 (Abstract Test 26)
     *
     * <pre>
     * Abstract Test 22: /ats/core/fc-response
     * Test Purpose: Validate that the Feature Collections complies with the require structure and contents.
     * Requirement: /req/core/fc-response
     *
     * Test Method
     *   7. If the numberReturned property is present, validate that it complies with /ats/core/fc-numberReturned
     * </pre>
     *
     * <pre>
     * Abstract Test 26: /ats/core/fc-numberReturned
     * Test Purpose: Validate the numberReturned parameter returned with a Features response
     * Requirement: /req/core/fc-numberReturned
     *
     * Test Method: Validate that the numberReturned value is identical to the number of features in the response.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Abstract Test 22, Test Method 5 (Requirement /req/core/fc-response) - Abstract Test 24 (Requirement /req/core/fc-timeStamp)", dataProvider = "collectionItemUris", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void validateGetFeaturesOperationResponse_NumberReturned( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        ResponseData response = collectionNameAndResponse.get( collectionId );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        JsonPath jsonPath = response.jsonPath();

        assertNumberReturned( collectionId, jsonPath, true );
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
     * @param testPoint
     *            the test point under test, never <code>null</code>
     * 
     */
    @Test(description = "Implements A.4.4.11. Limit Parameter (Requirement 18)", dataProvider = "collectionPaths", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void limitParameter( TestPoint testPoint ) {
        Parameter limit = retrieveParameterByName( testPoint.getPath(), apiModel, "limit" );

        assertNotNull( limit, "Required limit parameter for collections path '" + testPoint.getPath()
                              + "'  in OpenAPI document is missing" );

        String msg = "Expected property '%s' with value '%s' but was '%s'";

        assertEquals( limit.getName(), "limit", String.format( msg, "name", "limit", limit.getName() ) );
        assertEquals( limit.getIn(), "query", String.format( msg, "in", "query", limit.getIn() ) );
        assertFalse( isRequired( limit ), String.format( msg, "required", "false", limit.getRequired() ) );
        assertEquals( limit.getStyle(), "form", String.format( msg, "style", "form", limit.getStyle() ) );
        assertFalse( isExplode( limit ), String.format( msg, "explode", "false", limit.getExplode() ) );

        Schema schema = limit.getSchema();
        assertEquals( schema.getType(), "integer",
                      String.format( msg, "schema -> type", "integer", schema.getType() ) );
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
     */
    @Test(description = "Implements A.4.4.11. Limit Parameter (Requirement 19)", dataProvider = "collectionItemUrisWithLimit", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void limitParameter_requests( Map<String, Object> collection, int limit ) {
        String collectionId = (String) collection.get( "id" );

        String getFeaturesUrl = findFeaturesUrlForGeoJson( collection );
        if ( getFeaturesUrl == null || getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        ZonedDateTime timeStampBeforeResponse = ZonedDateTime.now();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).param( "limit",
                                                                                                limit ).when().request( GET );
        response.then().statusCode( 200 );
        ZonedDateTime timeStampAfterResponse = ZonedDateTime.now();

        JsonPath jsonPath = response.jsonPath();
        int numberOfFeatures = jsonPath.getList( "features" ).size();
        assertTrue( numberOfFeatures <= limit,
                    "Number of features for collection with name " + collectionId + " is unexpected (was "
                                               + numberOfFeatures + "), expected are " + limit + " or less" );
        assertTimeStamp( collectionId, jsonPath, timeStampBeforeResponse, timeStampAfterResponse, false );
        assertNumberReturned( collectionId, jsonPath, false );
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
     * @param testPoint
     *            the testPoint under test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.12. Bounding Box Parameter (Requirement 20)", dataProvider = "collectionPaths", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void boundingBoxParameter( TestPoint testPoint ) {
        Parameter bbox = retrieveParameterByName( testPoint.getPath(), apiModel, "bbox" );

        assertNotNull( bbox, "Required bbox parameter for collections path '" + testPoint.getPath()
                             + "'  in OpenAPI document is missing" );

        String msg = "Expected property '%s' with value '%s' for collections path '" + testPoint.getPath()
                     + "' but was '%s'.";

        assertEquals( bbox.getName(), "bbox", String.format( msg, "name", "bbox", bbox.getName() ) );
        assertEquals( bbox.getIn(), "query", String.format( msg, "in", "query", bbox.getIn() ) );
        assertFalse( isRequired( bbox ), String.format( msg, "required", "false", bbox.getRequired() ) );
        assertEquals( bbox.getStyle(), "form", String.format( msg, "style", "form", bbox.getStyle() ) );
        assertFalse( isExplode( bbox ), String.format( msg, "explode", "false", bbox.getExplode() ) );

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
    @Test(description = "Implements A.4.4.12. Bounding Box Parameter (Requirement 21)", dataProvider = "collectionItemUrisWithBboxes", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void boundingBoxParameter_requests( Map<String, Object> collection, BBox bbox )
                            throws URISyntaxException {
        String collectionId = (String) collection.get( "id" );

        String getFeaturesUrl = findFeaturesUrlForGeoJson( collection );
        if ( getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        ZonedDateTime timeStampBeforeResponse = ZonedDateTime.now();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).param( "bbox",
                                                                                                bbox.asQueryParameter() ).when().request( GET );
        response.then().statusCode( 200 );
        ZonedDateTime timeStampAfterResponse = ZonedDateTime.now();

        JsonPath jsonPath = response.jsonPath();
        assertTimeStamp( collectionId, jsonPath, timeStampBeforeResponse, timeStampAfterResponse, false );
        assertNumberReturned( collectionId, jsonPath, false );

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
     * @param testPoint
     *            the testPoint under test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.13. Time Parameter (Requirement 22)", dataProvider = "collectionPaths", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void datetimeParameter( TestPoint testPoint ) {
        Parameter time = retrieveParameterByName( testPoint.getPath(), apiModel, "datetime" );

        assertNotNull( time, "Required time parameter for collections with path '" + testPoint.getPath()
                             + "'  in OpenAPI document is missing" );

        String msg = "Expected property '%s' with value '%s' but was '%s'";

        assertEquals( time.getName(), "datetime", String.format( msg, "name", "datetime", time.getName() ) );
        assertEquals( time.getIn(), "query", String.format( msg, "in", "query", time.getIn() ) );
        assertFalse( isRequired( time ), String.format( msg, "required", "false", time.getRequired() ) );
        assertEquals( time.getStyle(), "form", String.format( msg, "style", "form", time.getStyle() ) );
        assertFalse( isExplode( time ), String.format( msg, "explode", "false", time.getExplode() ) );

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
     * @param queryParameter
     *            time parameter as string to use as query parameter, never <code>null</code>
     * @param begin
     *            a {@link ZonedDateTime} or {@link LocalDate}, the begin of the interval (or instant), never
     *            <code>null</code>
     * @param end
     *            a {@link ZonedDateTime} or {@link LocalDate}, the end of the interval, never <code>null</code> if the
     *            request is an instant
     * @throws URISyntaxException
     *             if the creation of a uri fails
     *
     */
    @Test(description = "Implements A.4.4.13. Time Parameter (Requirement 23)", dataProvider = "collectionItemUrisWithTimes", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void timeParameter_requests( Map<String, Object> collection, String queryParameter, Object begin,
                                        Object end )
                            throws URISyntaxException {
        String collectionName = (String) collection.get( "name" );

        String getFeaturesUrl = findFeaturesUrlForGeoJson( collection );
        if ( getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with name " + collectionName
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        ZonedDateTime timeStampBeforeResponse = ZonedDateTime.now();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).param( "time",
                                                                                                queryParameter ).when().request( GET );
        response.then().statusCode( 200 );
        ZonedDateTime timeStampAfterResponse = ZonedDateTime.now();

        JsonPath jsonPath = response.jsonPath();
        assertTimeStamp( collectionName, jsonPath, timeStampBeforeResponse, timeStampAfterResponse, false );
        assertNumberReturned( collectionName, jsonPath, false );

        // TODO: assert returned features
    }

    private void addFeatureIdToTestContext( ITestContext testContext, String collectionName, Response response ) {
        if ( response == null )
            return;
        Map<String, String> collectionNameAndFeatureId = (Map<String, String>) testContext.getSuite().getAttribute( SuiteAttribute.FEATUREIDS.getName() );
        if ( collectionNameAndFeatureId == null ) {
            collectionNameAndFeatureId = new HashMap<>();
            testContext.getSuite().setAttribute( SuiteAttribute.FEATUREIDS.getName(), collectionNameAndFeatureId );
        }
        String featureId = parseFeatureId( response.jsonPath() );
        if ( featureId != null )
            collectionNameAndFeatureId.put( collectionName, featureId );
    }

    private String findFeaturesUrlForGeoJson( Map<String, Object> collection ) {
        List<Object> links = (List<Object>) collection.get( "links" );
        for ( Object linkObject : links ) {
            Map<String, Object> link = (Map<String, Object>) linkObject;
            Object rel = link.get( "rel" );
            Object type = link.get( "type" );
            if ( "items".equals( rel ) && GEOJSON_MIME_TYPE.equals( type ) )
                return (String) link.get( "href" );
        }
        return null;
    }

    private boolean isRequired( Parameter param ) {
        return param.getRequired() != null && param.getRequired();
    }

    private Boolean isExplode( Parameter param ) {
        return param.getExplode() != null && param.getExplode();
    }

    private class ResponseData {

        private final Response response;

        private final ZonedDateTime timeStampBeforeResponse;

        private final ZonedDateTime timeStampAfterResponse;

        public ResponseData( Response response, ZonedDateTime timeStampBeforeResponse,
                             ZonedDateTime timeStampAfterResponse ) {
            this.response = response;
            this.timeStampBeforeResponse = timeStampBeforeResponse;
            this.timeStampAfterResponse = timeStampAfterResponse;
        }

        public JsonPath jsonPath() {
            return response.jsonPath();
        }
    }

}
