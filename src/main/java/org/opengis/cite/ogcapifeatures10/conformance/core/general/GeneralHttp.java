package org.opengis.cite.ogcapifeatures10.conformance.core.general;

import static io.restassured.http.Method.GET;
import static org.hamcrest.CoreMatchers.containsString;

import org.opengis.cite.ogcapifeatures10.conformance.CommonFixture;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * A.2.1. General Tests
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeneralHttp extends CommonFixture {

    /**
     * <pre>
     * A.2.1.1. HTTP
     *
     * Abstract Test 1: /ats/core/http
     * Test Purpose: Validate that the resource paths advertised through the API conform with HTTP 1.1 and, where
     * appropriate, TLS.
     * Requirement: /req/core/http
     *
     * Test Method:
     *  1. All compliance tests shall be configured to use the HTTP 1.1 protocol exclusively.
     *  2. For APIs which support HTTPS, all compliance tests shall be configured to use HTTP over TLS (RFC 2818) with
     * their HTTP 1.1 protocol. (untested)
     * </pre>
     */
    @Test(description = "Implements A.2.1.1. HTTP, Abstract Test 1 (Requirement /req/core/http)")
    public void http() {
        Response response = init().baseUri( rootUri.toString() ).when().request( GET, "/" );
        response.then().statusLine( containsString( "HTTP/1.1" ) );
    }

}
