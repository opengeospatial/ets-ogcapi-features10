package org.opengis.cite.ogcapifeatures10.apidescription;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapifeatures10.CommonFixture;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class LandingPage extends CommonFixture {

    private JsonPath response;

    /**
     * A.4.2.1. Landing Page Retrieval
     *
     * a) Test Purpose: Validate that a landing page can be retrieved from the expected location.
     *
     * b) Pre-conditions:
     *
     * A URL to the server hosting the landing page is known.
     *
     * The test client can authenticate to the server.
     *
     * The test client has sufficient privileges to access the landing page.
     *
     * c) Test Method:
     *
     * Issue an HTTP GET request to the URL {root}/
     *
     * Validate that a document was returned with a status code 200
     *
     * Validate the contents of the returned document using test A.4.2.2
     *
     * d) References: Requirement 1
     */
    @Test(description = "Implements A.4.2.1. Landing Page Retrieval (Requirement 1)", groups = "landingpage")
    public void landingPageRetrieval() {
        Response request = init().baseUri( rootUri.toString() ).accept( JSON ).when().request( GET, "/" );
        request.then().statusCode( 200 );
        response = request.jsonPath();
    }

    /**
     * A.4.2.2. Landing Page Validation
     *
     * a) Test Purpose: Validate that the landing page complies with the require structure and contents.
     *
     * b) Pre-conditions:
     *
     * The landing page has been retrieved from the server
     *
     * c) Test Method:
     *
     * Validate the landing page against the root.yaml schema
     *
     * Validate that the landing page includes a “service” link to API Definition
     *
     * Validate that the landing page includes a “conformance” link to the conformance class document
     *
     * Validate that the landing page includes a “data” link to the WFS contents.
     *
     * d) References: Requirement 2
     */
    @Test(description = "Implements A.4.2.2. Landing Page Validation (Requirement 2)", groups = "landingpage", dependsOnMethods = "landingPageRetrieval")
    public void landingPageValidation() {
        List<Object> links = response.getList( "links" );
        Set<String> linkTypes = collectLinkTypes( links );

        boolean expectedLinkTypesExists = ( linkTypes.contains( "service-desc" )
                                            || linkTypes.contains( "service-doc" ) )
                                          && linkTypes.contains( "conformance" ) && linkTypes.contains( "data" );
        assertTrue( expectedLinkTypesExists,
                    "The landing page must include at least links with relation type 'service', 'conformance' and 'data', but contains "
                                            + String.join( ", ", linkTypes ) );

    }

    private Set<String> collectLinkTypes( List<Object> links ) {
        Set<String> linkTypes = new HashSet<>();
        for ( Object link : links ) {
            Map<String, Object> linkMap = (Map<String, Object>) link;
            Object linkType = linkMap.get( "rel" );
            linkTypes.add( (String) linkType );
        }
        return linkTypes;
    }

}
