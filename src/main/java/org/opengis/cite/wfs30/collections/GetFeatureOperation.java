package org.opengis.cite.wfs30.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.wfs30.EtsAssert.assertTrue;
import static org.opengis.cite.wfs30.SuiteAttribute.API_MODEL;
import static org.opengis.cite.wfs30.WFS3.GEOJSON_MIME_TYPE;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinkByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.wfs30.util.JsonUtils.findUnsupportedTypes;
import static org.opengis.cite.wfs30.util.JsonUtils.linkIncludesRelAndType;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.wfs30.CommonDataFixture;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetFeatureOperation extends CommonDataFixture {

    private OpenApi3 apiModel;

    private List<Map<String, Object>> collections;

    private final Map<String, Response> collectionNameAndResponse = new HashMap<>();

    @DataProvider(name = "collectionFeatureId")
    public Iterator<Object[]> collectionFeatureId( ITestContext testContext ) {
        Map<String, String> collectionNameToFeatureId = (Map<String, String>) testContext.getSuite().getAttribute( SuiteAttribute.FEATUREIDS.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            String collectionName = (String) collection.get( "name" );
            String featureId = null;
            if ( collectionNameToFeatureId != null )
                featureId = collectionNameToFeatureId.get( collectionName );
            collectionsData.add( new Object[] { collection, featureId } );
        }
        return collectionsData.iterator();
    }

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
        this.collections = (List<Map<String, Object>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS.getName() );
    }

    /**
     * A.4.4.14. Get Feature Operation
     *
     * a) Test Purpose: Validate that the Get Feature Operation behaves as required.
     *
     * b) Pre-conditions:
     *
     * A feature collection name is provided by test A.4.4.6
     *
     * A feature identifier is provided by test A.4.4.10
     *
     * Path = /collections/{name}/items/(id} where {id} = the feature identifier
     *
     * c) Test Method:
     *
     * DO FOR each /collections{name}/items/{id} test point
     *
     * Issue an HTTP GET request using the test point URI
     *
     * Go to test A.4.4.15
     *
     * d) References: Requirement 30
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param featureId
     *            the featureId to request, may be <code>null</code> (test will be skipped)
     */
    @Test(description = "Implements A.4.4.14. Get Feature Operation (Requirement 30, 31)", dataProvider = "collectionFeatureId", dependsOnGroups = "getFeaturesBase", alwaysRun = true)
    public void getFeatureOperation( Map<String, Object> collection, String featureId ) {
        String collectionName = (String) collection.get( "name" );
        if ( featureId == null )
            throw new SkipException( "No featureId available for collection '" + collectionName + "'" );

        String getFeatureUrl = findGetFeatureUrlForGeoJson( collection );
        if ( getFeatureUrl == null )
            throw new SkipException( "Could not find url for collection with name " + collectionName
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        String getFeatureUrlWithFeatureId = getFeatureUrl.substring( 0, getFeatureUrl.indexOf( "?" ) ) + "/"
                                            + featureId;

        Response response = init().baseUri( getFeatureUrlWithFeatureId ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );

        collectionNameAndResponse.put( collectionName, response );
    }

    /**
     * A.4.4.15. Validate the Get Feature Operation Response
     *
     * a) Test Purpose: Validate the Get Feature Operation Response.
     *
     * b) Pre-conditions: The Feature has been retrieved from the server.
     *
     * c) Test Method:
     *
     * Validate the structure of the response as follows:
     *
     * For HTML use TBD
     *
     * For GeoJSON use featureGeoJSON.yaml
     *
     * For GML use featureGML.yaml
     *
     * Validate that the following links are included in the response document:
     *
     * To itself
     *
     * To the Feature Collection which contains this Feature
     *
     * Alternate encodings of this document in every other media type as identified by the compliance classes for this
     * server.
     *
     * Validate that each link includes a rel and type parameter.
     *
     * d) References: Requirements 31 and 32
     *
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param featureId
     *            the featureId to request, may be <code>null</code> (test will be skipped)
     */
    @Test(description = "Implements A.4.4.15. Validate the Get Feature Operation Response (Requirement 32)", dataProvider = "collectionFeatureId", dependsOnMethods = "getFeatureOperation", alwaysRun = true)
    public void validateTheGetFeatureOperationResponse( Map<String, Object> collection, String featureId ) {
        String collectionName = (String) collection.get( "name" );
        Response response = collectionNameAndResponse.get( collectionName );
        if ( response == null )
            throw new SkipException( "Could not find a response for collection with name " + collectionName );

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, Object>> links = jsonPath.getList( "links" );

        // Validate that the retrieved document includes links for: Itself
        Map<String, Object> linkToSelf = findLinkByRel( links, "self" );
        assertNotNull( linkToSelf, "Get Feature Operation Response must include a link for itself" );
        assertTrue( linkIncludesRelAndType( linkToSelf ), "Link to itself must include a rel and type parameter" );

        // Validate that the retrieved document includes links for:
        // To the Feature Collection which contains this Feature.
        Map<String, Object> linkToCollection = findLinkByRel( links, "collection" );
        assertNotNull( linkToCollection,
                       "Get Feature Operation Response must include a link for the feature collection" );
        assertTrue( linkIncludesRelAndType( linkToCollection ),
                    "Link to feature collection must include a rel and type parameter" );

        // Validate that the retrieved document includes links for:
        // Alternate encodings of this document in every other media type as identified by the compliance classes for
        // this server
        List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForFeatureCollectionsAndFeatures( linkToSelf );
        List<Map<String, Object>> alternateLinks = findLinksWithSupportedMediaTypeByRel( links, mediaTypesToSupport,
                                                                                         "alternate" );
        List<String> typesWithoutLink = findUnsupportedTypes( alternateLinks, mediaTypesToSupport );
        assertTrue( typesWithoutLink.isEmpty(),
                    "Get Feature Operation Response must include links for alternate encodings. Missing links for types "
                                            + typesWithoutLink );

        // Validate that each link includes a rel and type parameter.
        List<String> linksWithoutRelOrType = findLinksWithoutRelOrType( links );
        assertTrue( linksWithoutRelOrType.isEmpty(),
                    "Links for alternate encodings in Get Feature Operation Response must include a rel and type parameter. Missing for links "
                                            + linksWithoutRelOrType );
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

}