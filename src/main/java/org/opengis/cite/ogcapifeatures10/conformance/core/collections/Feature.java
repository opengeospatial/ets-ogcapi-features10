package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findFeatureUrlForGeoJson;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinkByRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findUnsupportedTypes;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.linkIncludesRelAndType;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseAsListOfMaps;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapifeatures10.conformance.CommonDataFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * A.2.8. Feature
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Feature extends CommonDataFixture {

    private List<Map<String, Object>> collections;

    private final Map<String, Response> collectionNameAndResponse = new HashMap<>();

    @DataProvider(name = "collectionFeatureId")
    public Iterator<Object[]> collectionFeatureId( ITestContext testContext ) {
        Map<String, String> collectionNameToFeatureId = (Map<String, String>) testContext.getSuite().getAttribute( SuiteAttribute.FEATUREIDS.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            String collectionId = (String) collection.get( "id" );
            String featureId = null;
            if ( collectionNameToFeatureId != null )
                featureId = collectionNameToFeatureId.get( collectionId );
            collectionsData.add( new Object[] { collection, featureId } );
        }
        return collectionsData.iterator();
    }

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.collections = (List<Map<String, Object>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS.getName() );
    }

    /**
     * <pre>
     * Abstract Test 27: /ats/core/f-op
     * Test Purpose: Validate that a feature can be retrieved from the expected location.
     * Requirement: /req/core/f-op
     *
     * Test Method
     *  1. For a sufficiently large subset of all features in a feature collection (path /collections/{collectionId}), issue an HTTP GET request to the URL /collections/{collectionId}/items/{featureId} where {collectionId} is the id property for the collection and {featureId} is the id property of the feature.
     *  2. Validate that a feature was returned with a status code 200
     *  3. Validate the contents of the returned feature using test /ats/core/f-success.
     * </pre>
     * 
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param featureId
     *            the featureId to request, may be <code>null</code> (test will be skipped)
     */
    @Test(description = "Implements A.2.8. Feature, Abstract Test 27 (Requirement /req/core/f-op)", dataProvider = "collectionFeatureId", dependsOnGroups = "featuresBase", alwaysRun = true)
    public void featureOperation( Map<String, Object> collection, String featureId ) {
        String collectionId = (String) collection.get( "id" );
        if ( featureId == null )
            throw new SkipException( "No featureId available for collection '" + collectionId + "'" );

        String getFeatureUrlWithFeatureId = JsonUtils.findFeatureUrlForGeoJson( rootUri, collection, featureId );
        if ( getFeatureUrlWithFeatureId == null )
            throw new SkipException( "Could not find url for collection with name " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );

        Response response = init().baseUri( getFeatureUrlWithFeatureId ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );

        collectionNameAndResponse.put( collectionId, response );
    }

    /**
     * <pre>
     * Abstract Test 28: /ats/core/f-success
     * Test Purpose: Validate that the Feature complies with the required structure and contents.
     * Requirement: /req/core/f-success
     *
     * Test Method
     *  1. Validate that the Feature includes all required link properties using /ats/core/f-links
     *  2. Validate the Feature for all supported media types using the resources and tests identified in Schema and Tests for Features
     * </pre>
     *
     * <pre>
     * Abstract Test 29: /ats/core/f-links
     * Test Purpose: Validate that the required links are included in a Feature.
     * Requirement: /req/core/f-links
     *
     * Test Method:
     * Verify that the returned Feature includes:
     *  1. a link to this response document (relation: self),
     *  2. a link to the response document in every other media type supported by the server (relation: alternate).
     *  3. a link to the feature collection that contains this feature (relation: collection).
     * Verify that all links include the rel and type link parameters.
     * </pre>
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param featureId
     *            the featureId to request, may be <code>null</code> (test will be skipped)
     */
    @Test(description = "Implements A.2.8. Feature, Abstract Test 28 + 29 (Requirements /req/core/f-success, /req/core/f-links)", dataProvider = "collectionFeatureId", dependsOnMethods = "featureOperation", alwaysRun = true)
    public void validateFeatureResponse( Map<String, Object> collection, String featureId ) {
        String collectionId = (String) collection.get( "id" );
        Response response = collectionNameAndResponse.get( collectionId );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> links = parseAsListOfMaps( "links", jsonPath );

        // 1. a link to this response document (relation: self),
        Map<String, Object> linkToSelf = findLinkByRel( links, "self" );
        assertNotNull( linkToSelf, "Feature Response must include a link for itself" );
        // Verify that all links include the rel and type link parameters.
        assertTrue( linkIncludesRelAndType( linkToSelf ), "Link to itself must include a rel and type parameter" );

        // 2. a link to the response document in every other media type supported by the server (relation: alternate).
        // Dev: Supported media type are identified by the compliance classes for this server
        List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForFeatureCollectionsAndFeatures( linkToSelf );
        List<Map<String, Object>> alternateLinks = findLinksWithSupportedMediaTypeByRel( links, mediaTypesToSupport,
                                                                                         "alternate" );
        List<String> typesWithoutLink = findUnsupportedTypes( alternateLinks, mediaTypesToSupport );
        assertTrue( typesWithoutLink.isEmpty(),
                    "Feature Response must include links for alternate encodings. Missing links for types "
                                                + typesWithoutLink );

        // 3. a link to the feature collection that contains this feature (relation: collection).
        Map<String, Object> linkToCollection = findLinkByRel( links, "collection" );
        assertNotNull( linkToCollection, "Feature Response must include a link for the feature collection" );
        assertTrue( linkIncludesRelAndType( linkToCollection ),
                    "Link to feature collection must include a rel and type parameter" );

        // Verify that all "self"/"alternate"/"collection" links include the rel and type link parameters.
        Set<String> rels = new HashSet<String>();
        rels.add( "self" );
        rels.add( "alternate" );
        rels.add( "collection" );
        List<String> linksWithoutRelOrType = findLinksWithoutRelOrType( links, rels );
        assertTrue( linksWithoutRelOrType.isEmpty(),
                    "Links with link relation types 'self', 'alternate' and 'collection' in Get Feature Operation Response must include a rel and type parameter. Missing for links "
                                                     + linksWithoutRelOrType );
    }

}