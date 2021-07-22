package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeaturesUrlForGeoJson;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseFeatureId;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * A.2.7. Features {root}/collections/{collectionId}/items
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Features extends AbstractFeatures {

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
     * </pre>
     *
     * @param testContext
     *            used to fill the FEATUREIDS, never <code>null</code>
     * @param collection
     *            the collection under test, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Abstract Test 13 (Requirement /req/core/fc-op)", groups = "featuresBase", dataProvider = "collectionItemUris", dependsOnGroups = "collections", alwaysRun = true)
    public void validateFeaturesOperation( ITestContext testContext, Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );

        String featuresUrl = findFeaturesUrlForGeoJson( rootUri, collection );
        if ( featuresUrl == null )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );

        ZonedDateTime timeStampBeforeResponse = ZonedDateTime.now();
        Response response = init().baseUri( featuresUrl ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );
        ZonedDateTime timeStampAfterResponse = ZonedDateTime.now();
        ResponseData responseData = new ResponseData( response, timeStampBeforeResponse, timeStampAfterResponse );
        collectionIdAndResponse.put( asKey( collectionId ), responseData );

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
    public void validateFeaturesResponse_TypeProperty( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        validateTypeProperty( asKey( collectionId ) );
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
    public void validateFeaturesResponse_FeaturesProperty( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        validateFeaturesProperty( asKey( collectionId ) );
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
    public void validateFeaturesResponse_Links( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        validateLinks( asKey( collectionId ) );
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
    public void validateFeaturesResponse_TimeStamp( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        validateTimeStamp( asKey( collectionId ) );
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
    public void validateFeaturesResponse_NumberMatched( Map<String, Object> collection )
                            throws URISyntaxException {
        String collectionId = (String) collection.get( "id" );
        validateNumberMatched( asKey( collectionId ) );
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
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items, Abstract Test 22, Test Method 7 (Requirement /req/core/fc-response) - Abstract Test 26 (Requirement /req/core/fc-numberReturned)", dataProvider = "collectionItemUris", dependsOnMethods = "validateFeaturesOperation", alwaysRun = true)
    public void validateFeaturesResponse_NumberReturned( Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        validateNumberReturned( asKey( collectionId ) );
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

    private CollectionResponseKey asKey( String collectionId ) {
        return new CollectionResponseKey( collectionId );
    }

}
