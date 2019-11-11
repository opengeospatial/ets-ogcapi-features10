package org.opengis.cite.ogcapifeatures10.general;

import static io.restassured.http.Method.GET;
import static org.hamcrest.CoreMatchers.containsString;

import org.opengis.cite.ogcapifeatures10.CommonFixture;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeneralHttp extends CommonFixture {

    /**
     * A.4.1.1. HTTP 1.1
     *
     * a) Test Purpose: Validate that the WFS services advertised through the API conform with HTTP 1.1.
     *
     * b) Pre-conditions: none
     *
     * c) Test Method:
     *
     * Build all requests using the HTTP 1.1 protocol.
     *
     * Validate that all responses comply with the HTTP 1.1 protocol
     *
     * d) References: Requirement 7
     */
    @Test(description = "Implements A.4.1.1. HTTP 1.1 (Requirement 7)")
    public void http11() {
        Response response = init().baseUri( rootUri.toString() ).when().request( GET, "/" );
        response.then().statusLine( containsString( "HTTP/1.1" ) );
    }

}
