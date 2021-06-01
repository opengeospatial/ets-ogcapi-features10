package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertFalse;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveParameterByName;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeaturesUrlForGeoJson;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.formatDate;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.formatDateRange;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.formatDateRangeWithDuration;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseTemporalExtent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.opengis.cite.ogcapifeatures10.util.TemporalExtent;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.Schema;

import io.restassured.response.Response;

/**
 * A.2.7. Features {root}/collections/{collectionId}/items - Datetime
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesTime extends AbstractFeatures {

    @DataProvider(name = "collectionItemUrisWithDateTimes")
    public Iterator<Object[]> collectionItemUrisWithDateTimes( ITestContext testContext ) {
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

    /**
     * <pre>
     * Abstract Test 18: /ats/core/fc-time-definition
     * Test Purpose: Validate that the dateTime query parameters are constructed correctly.
     * Requirement: /req/core/fc-time-definition
     *
     * Test Method: Verify that the datetime query parameter complies with the following definition (using an OpenAPI Specification 3.0 fragment):
     *
     * name: datetime
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
    @Test(description = "A.2.7. Features {root}/collections/{collectionId}/items - Datetime, Abstract Test 18: (Requirement /req/core/fc-time-definition)", dataProvider = "collectionPaths", dependsOnGroups = "featuresBase", alwaysRun = true)
    public void timeParameterDefinition( TestPoint testPoint ) {
        Parameter time = retrieveParameterByName( testPoint.getPath(), getApiModel(), "datetime" );

        assertNotNull( time, "Required datetime parameter for collections with path '" + testPoint.getPath()
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
     * DateTime:
     *   * Parameter /ats/core/fc-time-definition
     *   * Response /ats/core/fc-time-response
     * </pre>
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
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - DateTIme, Abstract Test 13: (Requirement /req/core/fc-op)", dataProvider = "collectionItemUrisWithDateTimes", dependsOnGroups = "featuresBase", alwaysRun = true)
    public void validateFeaturesWithDateTimeOperation( Map<String, Object> collection, String queryParameter,
                                                       Object begin, Object end ) {
        String collectionId = (String) collection.get( "id" );

        String getFeaturesUrl = findFeaturesUrlForGeoJson( rootUri, collection );
        if ( getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        ZonedDateTime timeStampBeforeResponse = ZonedDateTime.now();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).param( "datetime",
                                                                                                queryParameter ).when().request( GET );
        response.then().statusCode( 200 );
        ZonedDateTime timeStampAfterResponse = ZonedDateTime.now();
        ResponseData responseData = new ResponseData( response, timeStampBeforeResponse, timeStampAfterResponse );
        collectionIdAndResponse.put( asKey( collectionId, queryParameter ), responseData );

    }

    /**
     * <pre>
     * Abstract Test 19: /ats/core/fc-time-response
     * Test Purpose: Validate that the dataTime query parameters are processed correctly.
     * Requirement: /req/core/fc-time-response
     *
     * Test Method
     *   1. Verify that only features that have a temporal geometry that intersects the temporal information in the datetime parameter were included in the result set
     *   2. Verify that all features in the collection that are not associated with a temporal geometry are included in the result set
     *   3. Validate that the datetime parameter complies with the syntax described in /req/core/fc-time-response.
     * </pre>
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
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - DateTime, Abstract Test 19: (Requirement /req/core/fc-time-response)", dataProvider = "collectionItemUrisWithDateTimes", dependsOnGroups = "featuresBase", alwaysRun = true)
    public void validateFeaturesWithDateTimeResponse( Map<String, Object> collection, String queryParameter,
                                                      Object begin, Object end ) {
        String collectionId = (String) collection.get( "id" );
        ResponseData response = collectionIdAndResponse.get( asKey( collectionId, queryParameter ) );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        // TODO: assert returned features
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
     * @param queryParameter
     *            time parameter as string to use as query parameter, never <code>null</code>
     * @param begin
     *            a {@link ZonedDateTime} or {@link LocalDate}, the begin of the interval (or instant), never
     *            <code>null</code>
     * @param end
     *            a {@link ZonedDateTime} or {@link LocalDate}, the end of the interval, never <code>null</code> if the
     *            request is an instant
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - DateTime, Abstract Test 22, Test Method 1 (Requirement /req/core/fc-response)", dataProvider = "collectionItemUrisWithDateTimes", dependsOnMethods = "validateFeaturesWithDateTimeOperation", alwaysRun = true)
    public void validateFeaturesWithDateTimeResponse_TypeProperty( Map<String, Object> collection,
                                                                   String queryParameter, Object begin, Object end ) {
        String collectionId = (String) collection.get( "id" );
        validateTypeProperty( asKey( collectionId, queryParameter ) );
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
     * @param queryParameter
     *            time parameter as string to use as query parameter, never <code>null</code>
     * @param begin
     *            a {@link ZonedDateTime} or {@link LocalDate}, the begin of the interval (or instant), never
     *            <code>null</code>
     * @param end
     *            a {@link ZonedDateTime} or {@link LocalDate}, the end of the interval, never <code>null</code> if the
     *            request is an instant
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - DateTime, Abstract Test 22, Test Method 2 (Requirement /req/core/fc-response)", dataProvider = "collectionItemUrisWithDateTimes", dependsOnMethods = "validateFeaturesWithDateTimeOperation", alwaysRun = true)
    public void validateFeaturesWithDateTimeResponse_FeaturesProperty( Map<String, Object> collection,
                                                                       String queryParameter, Object begin,
                                                                       Object end ) {
        String collectionId = (String) collection.get( "id" );
        validateFeaturesProperty( asKey( collectionId, queryParameter ) );
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
     * @param queryParameter
     *            time parameter as string to use as query parameter, never <code>null</code>
     * @param begin
     *            a {@link ZonedDateTime} or {@link LocalDate}, the begin of the interval (or instant), never
     *            <code>null</code>
     * @param end
     *            a {@link ZonedDateTime} or {@link LocalDate}, the end of the interval, never <code>null</code> if the
     *            request is an instant
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - DateTime, Abstract Test 22, Test Method 4 (Requirement /req/core/fc-response) - Abstract Test 23 (Requirement /req/core/fc-links, /req/core/fc-rel-type)", dataProvider = "collectionItemUrisWithDateTimes", dependsOnMethods = "validateFeaturesWithDateTimeOperation", alwaysRun = true)
    public void validateFeaturesWithDateTimeResponse_Links( Map<String, Object> collection, String queryParameter,
                                                            Object begin, Object end ) {
        String collectionId = (String) collection.get( "id" );
        validateLinks( asKey( collectionId, queryParameter ) );
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
     * @param queryParameter
     *            time parameter as string to use as query parameter, never <code>null</code>
     * @param begin
     *            a {@link ZonedDateTime} or {@link LocalDate}, the begin of the interval (or instant), never
     *            <code>null</code>
     * @param end
     *            a {@link ZonedDateTime} or {@link LocalDate}, the end of the interval, never <code>null</code> if the
     *            request is an instant
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - DateTime, Abstract Test 22, Test Method 5 (Requirement /req/core/fc-response) - Abstract Test 24 (Requirement /req/core/fc-timeStamp)", dataProvider = "collectionItemUrisWithDateTimes", dependsOnMethods = "validateFeaturesWithDateTimeOperation", alwaysRun = true)
    public void validateFeaturesWithDateTimeResponse_TimeStamp( Map<String, Object> collection, String queryParameter,
                                                                Object begin, Object end ) {
        String collectionId = (String) collection.get( "id" );
        validateTimeStamp( asKey( collectionId, queryParameter ) );
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
     * @param queryParameter
     *            time parameter as string to use as query parameter, never <code>null</code>
     * @param begin
     *            a {@link ZonedDateTime} or {@link LocalDate}, the begin of the interval (or instant), never
     *            <code>null</code>
     * @param end
     *            a {@link ZonedDateTime} or {@link LocalDate}, the end of the interval, never <code>null</code> if the
     *            request is an instant
     *
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - DateTime, Abstract Test 22, Test Method 6 (Requirement /req/core/fc-response) - Abstract Test 25 (Requirement /req/core/fc-numberMatched)", dataProvider = "collectionItemUrisWithDateTimes", dependsOnMethods = "validateFeaturesWithDateTimeOperation", alwaysRun = true)
    public void validateFeaturesWithDateTimeResponse_NumberMatched( Map<String, Object> collection,
                                                                    String queryParameter, Object begin, Object end )
                            throws URISyntaxException {
        String collectionId = (String) collection.get( "id" );
        validateNumberMatched( asKey( collectionId, queryParameter ) );
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
     * @param queryParameter
     *            time parameter as string to use as query parameter, never <code>null</code>
     * @param begin
     *            a {@link ZonedDateTime} or {@link LocalDate}, the begin of the interval (or instant), never
     *            <code>null</code>
     * @param end
     *            a {@link ZonedDateTime} or {@link LocalDate}, the end of the interval, never <code>null</code> if the
     *            request is an instant
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - DateTime, Abstract Test 22, Test Method 7 (Requirement /req/core/fc-response) - Abstract Test 26 (Requirement /req/core/fc-numberReturned)", dataProvider = "collectionItemUrisWithDateTimes", dependsOnMethods = "validateFeaturesWithDateTimeOperation", alwaysRun = true)
    public void validateFeaturesResponse_NumberReturned( Map<String, Object> collection, String queryParameter,
                                                         Object begin, Object end ) {
        String collectionId = (String) collection.get( "id" );
        validateNumberReturned( asKey( collectionId, queryParameter ) );
    }

    private CollectionIdWithTimeKey asKey( String collectionId, String queryParameter ) {
        return new CollectionIdWithTimeKey( collectionId, queryParameter );
    }

    private class CollectionIdWithTimeKey extends CollectionResponseKey {

        String queryParameter;

        public CollectionIdWithTimeKey( String collectionId, String queryParameter ) {
            super( collectionId );
            this.queryParameter = queryParameter;
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o )
                return true;
            if ( o == null || getClass() != o.getClass() )
                return false;
            if ( !super.equals( o ) )
                return false;
            CollectionIdWithTimeKey that = (CollectionIdWithTimeKey) o;
            return Objects.equals( queryParameter, that.queryParameter );
        }

        @Override
        public int hashCode() {
            return Objects.hash( super.hashCode(), queryParameter );
        }
    }

}
