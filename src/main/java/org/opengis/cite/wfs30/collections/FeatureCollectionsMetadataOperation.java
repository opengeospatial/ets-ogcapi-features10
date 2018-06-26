package org.opengis.cite.wfs30.collections;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.wfs30.SuiteAttribute.API_MODEL;
import static org.opengis.cite.wfs30.WFS3.PATH.COLLECTIONS;
import static org.opengis.cite.wfs30.openapi3.OpenApiUtils.retrieveTestPoints;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.cite.wfs30.CommonFixture;
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
public class FeatureCollectionsMetadataOperation extends CommonFixture {

    private final Map<TestPoint, Response> testPointAndResponses = new HashMap<>();

    private final List<String> collectionNames = new ArrayList<>();

    @DataProvider(name = "collectionsUris")
    public Object[][] collectionsUris( ITestContext testContext ) {
        OpenApi3 apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
        List<TestPoint> testPoints = retrieveTestPoints( apiModel, COLLECTIONS );
        return new Object[][] { testPoints.toArray() };
    }

    @BeforeClass
    public void parseRequiredMetadata( ITestContext testContext ) {
        OpenApi3 apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
    }

    /**
     * A.4.4.4. Validate the Feature Collections Metadata Operation
     *
     * a) Test Purpose: Validate that the Feature Collections Metadata Operation behaves as required
     *
     * b) Pre-conditions: Path = /collections
     *
     * c) Test Method:
     *
     * DO FOR each /collections test point < Issue an HTTP GET request using the test point URI
     *
     * Go to test A.4.4.5
     *
     * d) References: Requirement 9
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     * @return the response of the collections operation, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.4. Validate the Feature Collections Metadata Operation (Requirement 9, Requirement 10)", dataProvider = "collectionsUris", dependsOnGroups = "apidefinition")
    public void validateFeatureCollectionsMetadataOperation( TestPoint testPoint ) {
        String testPointUri = testPoint.createUri();
        Response response = init().baseUri( testPointUri ).accept( JSON ).when().request( GET );
        response.then().statusCode( 200 );
        this.testPointAndResponses.put( testPoint, response );
    }

    /**
     * A.4.4.5. Validate the Feature Collections Metadata Operation Response (Part 1)
     *
     * a) Test Purpose: Validate that response to the Feature Collection Metadata Operation.
     *
     * b) Pre-conditions: A Feature Collection Metadata document has been retrieved
     *
     * c) Test Method:
     *
     * Validate that the retrieved document includes links for: Itself, Alternate encodings of this document in every
     * other media type as identified by the compliance classes for this server.
     *
     * Validate that each link includes a rel and type parameter
     *
     * d) References: Requirements 11
     * 
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.5. Validate the Feature Collections Metadata Operation Response (Requirement 11)", dataProvider = "collectionsUris", dependsOnMethods = "validateFeatureCollectionsMetadataOperation")
    public void validateFeatureCollectionsMetadataOperationResponse_Links( TestPoint testPoint ) {
        Response response = testPointAndResponses.get( testPoint );
        if ( response == null )
            throw new SkipException( "Could not find a response for test point " + testPoint );

        JsonPath jsonPath = response.jsonPath();

        // Validate that the retrieved document includes links for: Itself,
        Map<String, Object> linkToSelf = findLinkToItself( jsonPath );
        assertNotNull( linkToSelf, "Feature Collection Metadata document must include a link for itself" );
        assertTrue( linkIncludesRelAndType( linkToSelf ), "Link to itself must include a rel and type parameter" );

        // Validate that the retrieved document includes links for: Itself, Alternate encodings of this document in
        // every other media type as identified by the compliance classes for this server.
        Map<String, MediaType> contentMediaTypes = testPoint.getContentMediaTypes();
        List<Map<String, Object>> alternateLinks = findAlternateLinks( jsonPath, linkToSelf, contentMediaTypes );
        List<String> typesWithoutLink = findLinksWithoutTypes( alternateLinks, contentMediaTypes );
        assertTrue( typesWithoutLink.isEmpty(),
                    "Feature Collection Metadata document must include links for alternate encodings. Missing links for types "
                                            + typesWithoutLink );

        List<String> linksWithoutRelOrType = findLinksWithoutRelOrType( alternateLinks );
        assertTrue( linksWithoutRelOrType.isEmpty(),
                    "Links for alternate encodings must include a rel and type parameter. Missing for links "
                                            + linksWithoutRelOrType );
    }

    /**
     * A.4.4.5. Validate the Feature Collections Metadata Operation Response (Part 1)
     *
     * a) Test Purpose: Validate that response to the Feature Collection Metadata Operation.
     *
     * b) Pre-conditions: A Feature Collection Metadata document has been retrieved
     *
     * c) Test Method:
     *
     * Validate that the returned document includes a collections property for each collection in the dataset.
     *
     * d) References: Requirements 12
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.5. Validate the Feature Collections Metadata Operation Response (Requirement 12)", dataProvider = "collectionsUris", dependsOnMethods = "validateFeatureCollectionsMetadataOperation")
    public void validateFeatureCollectionsMetadataOperationResponse_Collections( TestPoint testPoint ) {
        Response response = testPointAndResponses.get( testPoint );
        if ( response == null )
            throw new SkipException( "Could not find a response for test point " + testPoint );
        // TODO:
    }

    private Map<String, Object> findLinkToItself( JsonPath jsonPath ) {
        List<Object> links = jsonPath.getList( "links" );
        for ( Object link : links ) {
            Map<String, Object> linkItem = (Map<String, Object>) link;
            Object rel = linkItem.get( "rel" );
            if ( "self".equals( rel ) )
                return linkItem;
        }
        return null;
    }

    private List<Map<String, Object>> findAlternateLinks( JsonPath jsonPath, Map<String, Object> linkToSelf,
                                                          Map<String, MediaType> contentMediaTypes ) {
        Object typeOfSelf = linkToSelf.get( "type" );
        List<Map<String, Object>> alternateLinks = new ArrayList<>();
        List<Object> links = jsonPath.getList( "links" );
        for ( Object link : links ) {
            Map<String, Object> linkItem = (Map<String, Object>) link;
            Object type = linkItem.get( "type" );
            Object rel = linkItem.get( "rel" );
            if ( !typeOfSelf.equals( type ) && "alternate".equals( rel )
                 && isSupportedMediaType( type, contentMediaTypes ) )
                alternateLinks.add( linkItem );
        }
        return alternateLinks;
    }

    private List<String> findLinksWithoutTypes( List<Map<String, Object>> alternateLinks,
                                                Map<String, MediaType> contentMediaTypes ) {
        List<String> missingLinksForType = new ArrayList<>();
        for ( String contentMediaType : contentMediaTypes.keySet() ) {
            boolean hasLinkForContentType = hasLinkForContentType( alternateLinks, contentMediaType );
            if ( !hasLinkForContentType )
                missingLinksForType.add( contentMediaType );

        }
        return missingLinksForType;
    }

    private boolean hasLinkForContentType( List<Map<String, Object>> alternateLinks, String mediaType ) {
        for ( Map<String, Object> alternateLink : alternateLinks ) {
            Object type = alternateLink.get( "type" );
            if ( mediaType.equals( type ) )
                return true;
        }
        return false;
    }

    private List<String> findLinksWithoutRelOrType( List<Map<String, Object>> alternateLinks ) {
        List<String> linksWithoutRelOrType = new ArrayList<>();
        for ( Map<String, Object> alternateLink : alternateLinks ) {
            if ( !linkIncludesRelAndType( alternateLink ) )
                linksWithoutRelOrType.add( (String) alternateLink.get( "href" ) );
        }
        return linksWithoutRelOrType;
    }

    private boolean linkIncludesRelAndType( Map<String, Object> link ) {
        Object rel = link.get( "rel" );
        Object type = link.get( "type" );
        if ( rel != null && type != null )
            return true;
        return false;
    }

    private boolean isSupportedMediaType( Object type, Map<String, MediaType> contentMediaTypes ) {
        for ( String contentMediaType : contentMediaTypes.keySet() ) {
            if ( contentMediaType.equals( type ) )
                return true;
        }
        return false;
    }
}
