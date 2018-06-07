package org.opengis.cite.wfs30.apidescription;

import static io.restassured.http.Method.GET;

import java.net.MalformedURLException;

import org.opengis.cite.wfs30.CommonFixture;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class LandingPage extends CommonFixture {

    private String response;

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
    @Test(description = "Implements A.4.2.1. (Requirement 1: API Landing Page Operation)")
    public void landingPageRetrieval() {
        Response request = init().baseUri( rootUri.toString() ).params( "f", "json" ).when().request( GET, "/" );
        request.then().statusCode( 200 );
        response = request.asString();
    }

    @Test(description = "Implements A.4.2.1. (Requirement 1: API Landing Page Operation)")
    public void landingPageRetrievalFail() {
        init().when().request( GET, "/" ).then().statusCode( 400 );
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
    @Test(description = "Implements A.4.2.2. (Requirement 2: API Landing Page Validation)", dependsOnMethods = "landingPageRetrieval")
    public void landingPageValidation()
                            throws MalformedURLException {
    }

}
