package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertFalse;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.IUT;
import static org.opengis.cite.ogcapifeatures10.conformance.core.collections.FeaturesAssertions.assertIntegerGreaterZero;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveParameterByName;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollection;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeaturesUrlForGeoJson;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseAsList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.Schema;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * A.2.7. Features {root}/collections/{collectionId}/items - Limit
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesLimit extends AbstractFeatures {

    @DataProvider(name = "collectionItemUrisWithLimits")
    public Iterator<Object[]> collectionItemUrisWithLimits( ITestContext testContext ) {
        URI iut = (URI) testContext.getSuite().getAttribute( IUT.getName() );
        List<Object[]> collectionsWithLimits = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            String collectionId = (String) collection.get( "id" );
            List<TestPoint> testPoints = retrieveTestPointsForCollection( getApiModel(), iut, collectionId );
            for ( TestPoint testPoint : testPoints ) {
                Parameter limit = retrieveParameterByName( testPoint.getPath(), getApiModel(), "limit" );
                if ( limit != null && limit.getSchema() != null ) {
                    int min = limit.getSchema().getMinimum().intValue();
                    int max = limit.getSchema().getMaximum().intValue();
                    if ( min == max ) {
                        collectionsWithLimits.add( new Object[] { collection, min, max } );
                    } else {
                        collectionsWithLimits.add( new Object[] { collection, min, max } );
                        int betweenMinAndMax = min + ((max - min) / 2) > 100 ? 100 : min + ((max - min) / 2);
                        collectionsWithLimits.add( new Object[] { collection, betweenMinAndMax, max } );
                    }
                }
            }
        }
        return collectionsWithLimits.iterator();
    }

    /**
     * <pre>
     * Abstract Test 16: /ats/core/fc-limit-definition
     * Test Purpose: Validate that the bounding box query parameters are constructed corrrectly.
     * Requirement: /req/core/fc-limit-definition
     *
     * Test Method: Verify that the limit query parameter complies with the following definition (using an OpenAPI Specification 3.0 fragment):
     *
     * name: limit
     * in: query
     * required: false
     * schema:
     *   type: integer
     * style: form
     * explode: false
     *
     * Note that the API can define values for "minimum", "maximum" and "default".
     * </pre>
     *
     * @param testPoint
     *            the testPoint under test, never <code>null</code>
     */
    @Test(description = "A.2.7. Features {root}/collections/{collectionId}/items - Limit, Abstract Test 16: (Requirement /req/core/fc-limit-definition)", dataProvider = "collectionPaths", dependsOnGroups = "featuresBase", alwaysRun = true)
    public void limitParameterDefinition( TestPoint testPoint ) {
        Parameter limit = retrieveParameterByName( testPoint.getPath(), getApiModel(), "limit" );

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
        assertIntegerGreaterZero( schema.getMinimum(), "schema -> minimum" );
        assertIntegerGreaterZero( schema.getDefault(), "schema -> default" );
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
     * Limit:
     *   * Parameter /ats/core/fc-limit-definition
     *   * Response /ats/core/fc-limit-response
     * </pre>
     * 
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param limit
     *            limit parameter to request, never <code>null</code>
     * @param max
     *            max limit defined by the service, never <code>null</code>
     */
    @Test(description = "A.2.7. Features {root}/collections/{collectionId}/items - Limit, Abstract Test 13: (Requirement /req/core/fc-op)", dataProvider = "collectionItemUrisWithLimits", dependsOnGroups = "featuresBase", alwaysRun = true)
    public void validateFeaturesWithLimitOperation( Map<String, Object> collection, int limit, int max ) {
        String collectionId = (String) collection.get( "id" );

        String getFeaturesUrl = findFeaturesUrlForGeoJson( rootUri, collection );
        if ( getFeaturesUrl == null || getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        ZonedDateTime timeStampBeforeResponse = ZonedDateTime.now();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).param( "limit",
                                                                                                limit ).when().request( GET );
        response.then().statusCode( 200 );
        ZonedDateTime timeStampAfterResponse = ZonedDateTime.now();
        ResponseData responseData = new ResponseData( response, timeStampBeforeResponse, timeStampAfterResponse );
        collectionIdAndResponse.put( asKey( collectionId, limit ), responseData );
    }

    /**
     * <pre>
     * Abstract Test 17: /ats/core/fc-limit-response
     * Test Purpose: Validate that the limit query parameters are processed correctly.
     * Requirement: /req/core/fc-limit-response
     *
     * Test Method
     *  1. Count the Features which are on the first level of the collection. Any nested objects contained within the explicitly requested items are not be counted.
     *  2. Verify that this count is not greater than the value specified by the limit parameter.
     *  3. If the API definition specifies a maximum value for limit parameter, verify that the count does not exceed this maximum value.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param limit
     *            limit parameter to request, never <code>null</code>
     * @param max
     *            max limit defined by the service, never <code>null</code>
     */
    @Test(description = "A.2.7. Features {root}/collections/{collectionId}/items - Limit, Abstract Test 17: (Requirement /req/core/fc-limit-response)", dataProvider = "collectionItemUrisWithLimits", dependsOnMethods = "validateFeaturesWithLimitOperation", alwaysRun = true)
    public void validateFeaturesWithLimitResponse( Map<String, Object> collection, int limit, int max ) {
        String collectionId = (String) collection.get( "id" );
        ResponseData response = collectionIdAndResponse.get( asKey( collectionId, limit ) );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        JsonPath jsonPath = response.jsonPath();
        int numberOfFeatures = parseAsList( "features", jsonPath ).size();
        int expectedLimit = limit > max ? max : limit;
        assertTrue( numberOfFeatures <= expectedLimit,
                    "Number of features for collection with name " + collectionId + " is unexpected (was "
                                                       + numberOfFeatures + "), expected are " + limit + " or less" );
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
     * @param limit
     *            limit parameter to request, never <code>null</code>
     * @param max
     *            max limit defined by the service, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Limit, Abstract Test 22, Test Method 1 (Requirement /req/core/fc-response)", dataProvider = "collectionItemUrisWithLimits", dependsOnMethods = "validateFeaturesWithLimitOperation", alwaysRun = true)
    public void validateFeaturesWithLimitResponse_TypeProperty( Map<String, Object> collection, int limit, int max ) {
        String collectionId = (String) collection.get( "id" );
        validateTypeProperty( asKey( collectionId, limit ) );
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
     * @param limit
     *            limit parameter to request, never <code>null</code>
     * @param max
     *            max limit defined by the service, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Limit, Abstract Test 22, Test Method 2 (Requirement /req/core/fc-response)", dataProvider = "collectionItemUrisWithLimits", dependsOnMethods = "validateFeaturesWithLimitOperation", alwaysRun = true)
    public void validateFeaturesWithLimitResponse_FeaturesProperty( Map<String, Object> collection, int limit,
                                                                    int max ) {
        String collectionId = (String) collection.get( "id" );
        validateFeaturesProperty( asKey( collectionId, limit ) );
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
     * @param limit
     *            limit parameter to request, never <code>null</code>
     * @param max
     *            max limit defined by the service, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Limit, Abstract Test 22, Test Method 4 (Requirement /req/core/fc-response) - Abstract Test 23 (Requirement /req/core/fc-links, /req/core/fc-rel-type)", dataProvider = "collectionItemUrisWithLimits", dependsOnMethods = "validateFeaturesWithLimitOperation", alwaysRun = true)
    public void validateFeaturesWithLimitResponse_Links( Map<String, Object> collection, int limit, int max ) {
        String collectionId = (String) collection.get( "id" );
        validateLinks( asKey( collectionId, limit ) );
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
     * @param limit
     *            limit parameter to request, never <code>null</code>
     * @param max
     *            max limit defined by the service, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Limit, Abstract Test 22, Test Method 5 (Requirement /req/core/fc-response) - Abstract Test 24 (Requirement /req/core/fc-timeStamp)", dataProvider = "collectionItemUrisWithLimits", dependsOnMethods = "validateFeaturesWithLimitOperation", alwaysRun = true)
    public void validateFeaturesWithLimitResponse_TimeStamp( Map<String, Object> collection, int limit, int max ) {
        String collectionId = (String) collection.get( "id" );
        validateTimeStamp( asKey( collectionId, limit ) );
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
     * @param limit
     *            limit parameter to request, never <code>null</code>
     * @param max
     *            max limit defined by the service, never <code>null</code>
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Limit, Abstract Test 22, Test Method 6 (Requirement /req/core/fc-response) - Abstract Test 25 (Requirement /req/core/fc-numberMatched)", dataProvider = "collectionItemUrisWithLimits", dependsOnMethods = "validateFeaturesWithLimitOperation", alwaysRun = true)
    public void validateFeaturesWithLimitResponse_NumberMatched( Map<String, Object> collection, int limit, int max )
                            throws URISyntaxException {
        String collectionId = (String) collection.get( "id" );
        validateNumberMatched( asKey( collectionId, limit ) );
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
     * @param limit
     *            limit parameter to request, never <code>null</code>
     * @param max
     *            max limit defined by the service, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Limit, Abstract Test 22, Test Method 7 (Requirement /req/core/fc-response) - Abstract Test 26 (Requirement /req/core/fc-numberReturned)", dataProvider = "collectionItemUrisWithLimits", dependsOnMethods = "validateFeaturesWithLimitOperation", alwaysRun = true)
    public void validateFeaturesResponse_NumberReturned( Map<String, Object> collection, int limit, int max ) {
        String collectionId = (String) collection.get( "id" );
        validateNumberReturned( asKey( collectionId, limit ) );
    }

    private CollectionIdWithLimitKey asKey( String collectionId, int limit ) {
        return new CollectionIdWithLimitKey( collectionId, limit );
    }

    private class CollectionIdWithLimitKey extends CollectionResponseKey {

        int limit;

        public CollectionIdWithLimitKey( String collectionId, int limit ) {
            super( collectionId );
            this.limit = limit;
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o )
                return true;
            if ( o == null || getClass() != o.getClass() )
                return false;
            if ( !super.equals( o ) )
                return false;
            CollectionIdWithLimitKey that = (CollectionIdWithLimitKey) o;
            return limit == that.limit;
        }

        @Override
        public int hashCode() {
            return Objects.hash( super.hashCode(), limit );
        }
    }

}
