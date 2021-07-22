package org.opengis.cite.ogcapifeatures10.conformance.core.landingpage;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapifeatures10.conformance.CommonFixture;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * A.2.2. Landing Page {root}/
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class LandingPage extends CommonFixture {

    private JsonPath response;

    /**
     * <pre>
     * Abstract Test 3: /ats/core/root-op
     * Test Purpose: Validate that a landing page can be retrieved from the expected location.
     * Requirement: /req/core/root-op
     *
     * Test Method:
     *  1. Issue an HTTP GET request to the URL {root}/
     *  2. Validate that a document was returned with a status code 200
     *  3. Validate the contents of the returned document using test /ats/core/root-success.
     * </pre>
     */
    @Test(description = "Implements A.2.2. Landing Page {root}/, Abstract Test 3 (Requirement /req/core/root-op)", groups = "landingpage")
    public void landingPageRetrieval() {
        Response request = init().baseUri( rootUri.toString() ).accept( JSON ).when().request( GET, "/" );
        request.then().statusCode( 200 );
        response = request.jsonPath();
    }

    /**
     * <pre>
     * Abstract Test 4: /ats/core/root-success
     * Test Purpose: Validate that the landing page complies with the require structure and contents.
     * Requirement: /req/core/root-success
     *
     * Test Method: Validate the landing page for all supported media types using the resources and tests identified in
     * Schema and Tests for Landing Pages. For formats that require manual inspection, perform the following: a)
     * Validate that the landing page includes a "service-desc" and/or "service-doc" link to an API Definition b)
     * Validate that the landing page includes a "conformance" link to the conformance class declaration c) Validate
     * that the landing page includes a "data" link to the Feature contents.
     * </pre>
     */
    @Test(description = "Implements A.2.2. Landing Page {root}/, Abstract Test 4 (Requirement /req/core/root-success)", groups = "landingpage", dependsOnMethods = "landingPageRetrieval")
    public void landingPageValidation() {
        List<Object> links = response.getList( "links" );
        Set<String> linkTypes = collectLinkTypes( links );

        boolean expectedLinkTypesExists = ( linkTypes.contains( "service-desc" )
                                            || linkTypes.contains( "service-doc" ) )
                                          && linkTypes.contains( "conformance" ) && linkTypes.contains( "data" );
        assertTrue( expectedLinkTypesExists,
                    "The landing page must include at least links with relation type 'service-desc' or 'service-doc', 'conformance' and 'data', but contains "
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