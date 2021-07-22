package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertFalse;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveParameterByName;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeaturesUrlForGeoJson;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseSpatialExtent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.opengis.cite.ogcapifeatures10.util.BBox;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.Schema;

import io.restassured.response.Response;

/**
 * A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesBBox extends AbstractFeatures {

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
                collectionsWithBboxes.add( new Object[] { collection, new BBox( -180.0, -90.0, 180.0, -85.0 ) } );
            }
        }
        return collectionsWithBboxes.iterator();
    }

    /**
     * <pre>
     * Abstract Test 14: /ats/core/fc-bbox-definition
     * Test Purpose: Validate that the bounding box query parameters are constructed correctly.
     * Requirement: /req/core/fc-bbox-definition
     *
     * Test Method: Verify that the bbox query parameter complies with the following definition (using an OpenAPI Specification 3.0 fragment):
     *
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
     *
     * Use a bounding box with four numbers in all requests:
     *  * Lower left corner, WGS 84 longitude
     *  * Lower left corner, WGS 84 latitude
     *  * Upper right corner, WGS 84 longitude
     *  * Upper right corner, WGS 84 latitude
     * </pre>
     *
     * @param testPoint
     *            the testPoint under test, never <code>null</code>
     */
    @Test(description = "A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox, Abstract Test 14: (Requirement /req/core/fc-bbox-definition)", dataProvider = "collectionPaths", dependsOnGroups = "featuresBase", alwaysRun = true)
    public void boundingBoxParameterDefinition( TestPoint testPoint ) {
        Parameter bbox = retrieveParameterByName( testPoint.getPath(), getApiModel(), "bbox" );

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
        assertNotNull( schema, "Expected schema for bbox parameter for collections path '" + testPoint.getPath() );
        assertEquals( schema.getType(), "array", String.format( msg, "schema -> type", "array", schema.getType() ) );

        assertNotNull( schema.getMinItems(), String.format( msg, "schema -> minItems", "null", schema.getMinItems() ) );
        assertEquals( schema.getMinItems().intValue(), 4,
                      String.format( msg, "schema -> minItems", "4", schema.getMinItems() ) );

        assertNotNull( schema.getMaxItems(), String.format( msg, "schema -> maxItems", "null", schema.getMaxItems() ) );
        assertEquals( schema.getMaxItems().intValue(), 6,
                      String.format( msg, "schema -> maxItems", "6", schema.getMaxItems() ) );

        String itemsType = schema.getItemsSchema().getType();
        assertEquals( itemsType, "number", String.format( msg, "schema -> items -> type", "number", itemsType ) );
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
     * Bounding Box:
     *   * Parameter /ats/core/fc-bbox-definition
     *   * Response /ats/core/fc-bbox-response
     * </pre>
     * 
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param bbox
     *            bbox parameter to request, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox, Abstract Test 13: (Requirement /req/core/fc-op)", dataProvider = "collectionItemUrisWithBboxes", dependsOnGroups = "featuresBase", alwaysRun = true)
    public void validateFeaturesWithBoundingBoxOperation( Map<String, Object> collection, BBox bbox ) {
        String collectionId = (String) collection.get( "id" );

        String getFeaturesUrl = findFeaturesUrlForGeoJson( rootUri, collection );
        if ( getFeaturesUrl.isEmpty() )
            throw new SkipException( "Could not find url for collection with id " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        ZonedDateTime timeStampBeforeResponse = ZonedDateTime.now();
        Response response = init().baseUri( getFeaturesUrl ).accept( GEOJSON_MIME_TYPE ).param( "bbox",
                                                                                                bbox.asQueryParameter() ).when().request( GET );
        response.then().statusCode( 200 );
        ZonedDateTime timeStampAfterResponse = ZonedDateTime.now();
        ResponseData responseData = new ResponseData( response, timeStampBeforeResponse, timeStampAfterResponse );
        collectionIdAndResponse.put( asKey( collectionId, bbox ), responseData );
    }

    /**
     * <pre>
     * Abstract Test 15: /ats/core/fc-bbox-response
     * Test Purpose: Validate that the bounding box query parameters are processed corrrectly.
     * Requirement: /req/core/fc-bbox-response
     *
     * Test Method
     *   1. Verify that only features that have a spatial geometry that intersects the bounding box are returned as part of the result set.
     *   2. Verify that the bbox parameter matched all features in the collection that were not associated with a spatial geometry (this is only applicable for datasets that include features without a spatial geometry).
     *   3.  Verify that the coordinate reference system of the geometries is WGS 84 longitude/latitude ("http://www.opengis.net/def/crs/OGC/1.3/CRS84" or "http://www.opengis.net/def/crs/OGC/0/CRS84h") since no parameter bbox-crs was specified in the request.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param bbox
     *            bbox parameter to request, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox, Abstract Test 15: (Requirement /req/core/fc-bbox-response)", dataProvider = "collectionItemUrisWithBboxes", dependsOnMethods = "validateFeaturesWithBoundingBoxOperation", alwaysRun = true)
    public void validateFeaturesWithBoundingBoxResponse( Map<String, Object> collection, BBox bbox ) {
        String collectionId = (String) collection.get( "id" );
        ResponseData response = collectionIdAndResponse.get( asKey( collectionId, bbox ) );
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
     * @param bbox
     *            bbox parameter to request, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox, Abstract Test 22, Test Method 1 (Requirement /req/core/fc-response)", dataProvider = "collectionItemUrisWithBboxes", dependsOnMethods = "validateFeaturesWithBoundingBoxOperation", alwaysRun = true)
    public void validateFeaturesWithBoundingBoxResponse_TypeProperty( Map<String, Object> collection, BBox bbox ) {
        String collectionId = (String) collection.get( "id" );
        validateTypeProperty( asKey( collectionId, bbox ) );
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
     * @param bbox
     *            bbox parameter to request, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox, Abstract Test 22, Test Method 2 (Requirement /req/core/fc-response)", dataProvider = "collectionItemUrisWithBboxes", dependsOnMethods = "validateFeaturesWithBoundingBoxOperation", alwaysRun = true)
    public void validateFeaturesWithBoundingBoxResponse_FeaturesProperty( Map<String, Object> collection, BBox bbox ) {
        String collectionId = (String) collection.get( "id" );
        validateFeaturesProperty( asKey( collectionId, bbox ) );
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
     * @param bbox
     *            bbox parameter to request, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox, Abstract Test 22, Test Method 4 (Requirement /req/core/fc-response) - Abstract Test 23 (Requirement /req/core/fc-links, /req/core/fc-rel-type)", dataProvider = "collectionItemUrisWithBboxes", dependsOnMethods = "validateFeaturesWithBoundingBoxOperation", alwaysRun = true)
    public void validateFeaturesWithBoundingBoxResponse_Links( Map<String, Object> collection, BBox bbox ) {
        String collectionId = (String) collection.get( "id" );
        validateLinks( asKey( collectionId, bbox ) );
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
     * @param bbox
     *            bbox parameter to request, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox, Abstract Test 22, Test Method 5 (Requirement /req/core/fc-response) - Abstract Test 24 (Requirement /req/core/fc-timeStamp)", dataProvider = "collectionItemUrisWithBboxes", dependsOnMethods = "validateFeaturesWithBoundingBoxOperation", alwaysRun = true)
    public void validateFeaturesWithBoundingBoxResponse_TimeStamp( Map<String, Object> collection, BBox bbox ) {
        String collectionId = (String) collection.get( "id" );
        validateTimeStamp( asKey( collectionId, bbox ) );
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
     * @param bbox
     *            bbox parameter to request, never <code>null</code>
     *
     * @throws URISyntaxException
     *             if the creation of a uri fails
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox, Abstract Test 22, Test Method 6 (Requirement /req/core/fc-response) - Abstract Test 25 (Requirement /req/core/fc-numberMatched)", dataProvider = "collectionItemUrisWithBboxes", dependsOnMethods = "validateFeaturesWithBoundingBoxOperation", alwaysRun = true)
    public void validateFeaturesWithBoundingBoxResponse_NumberMatched( Map<String, Object> collection, BBox bbox )
                            throws URISyntaxException {
        String collectionId = (String) collection.get( "id" );
        validateNumberMatched( asKey( collectionId, bbox ) );
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
     * @param bbox
     *            bbox parameter to request, never <code>null</code>
     */
    @Test(description = "Implements A.2.7. Features {root}/collections/{collectionId}/items - BoundingBox, Abstract Test 22, Test Method 7 (Requirement /req/core/fc-response) - Abstract Test 26 (Requirement /req/core/fc-numberReturned)", dataProvider = "collectionItemUrisWithBboxes", dependsOnMethods = "validateFeaturesWithBoundingBoxOperation", alwaysRun = true)
    public void validateFeaturesResponse_NumberReturned( Map<String, Object> collection, BBox bbox ) {
        String collectionId = (String) collection.get( "id" );
        validateNumberReturned( asKey( collectionId, bbox ) );
    }

    private CollectionIdWithBboxKey asKey( String collectionId, BBox bBox ) {
        return new CollectionIdWithBboxKey( collectionId, bBox );
    }

    private class CollectionIdWithBboxKey extends CollectionResponseKey {

        BBox bbox;

        public CollectionIdWithBboxKey( String collectionId, BBox bbox ) {
            super( collectionId );
            this.bbox = bbox;
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o )
                return true;
            if ( o == null || getClass() != o.getClass() )
                return false;
            if ( !super.equals( o ) )
                return false;
            CollectionIdWithBboxKey that = (CollectionIdWithBboxKey) o;
            return Objects.equals( bbox, that.bbox );
        }

        @Override
        public int hashCode() {
            return Objects.hash( super.hashCode(), bbox );
        }
    }
}
