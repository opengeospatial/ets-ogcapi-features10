package org.opengis.cite.wfs30.apidescription;

import static io.restassured.RestAssured.given;
import static io.restassured.http.Method.GET;

import io.restassured.specification.RequestSpecification;
import org.opengis.cite.wfs30.CommonFixture;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class LandingPage extends CommonFixture {

    /**
     * A.4.2.1. Landing Page Retrieval a) Test Purpose: Validate that a landing page can be retrieved from the expected
     * location. b) Pre-conditions: A URL to the server hosting the landing page is known. The test client can
     * authenticate to the server. The test client has sufficient privileges to access the landing page. c) Test Method:
     * Issue an HTTP GET request to the URL {root}/ Validate that a document was returned with a status code 200
     * Validate the contents of the returned document using test A.4.2.2
     */
    @Test(description = "Implements A.4.2.1. (Requirement 1: API Landing Page Operation)")
    public void landingPageRetrieval() {
        init().when().request( GET,
                               "/" ).then().statusCode( 200 );
    }

    @Test(description = "Implements A.4.2.1. (Requirement 1: API Landing Page Operation)")
    public void landingPageRetrievalFail() {
        init().when().request( GET,
                               "/" ).then().statusCode( 400 );
    }
}
