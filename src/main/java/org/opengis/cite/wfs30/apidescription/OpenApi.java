package org.opengis.cite.wfs30.apidescription;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.wfs30.SuiteAttribute.API_MODEL;
import static org.opengis.cite.wfs30.WFS3.OPEN_API_MIME_TYPE;
import static org.testng.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.opengis.cite.wfs30.CommonFixture;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.val.ValidationResults;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class OpenApi extends CommonFixture {

    private String response;

    private String apiUrl;

    @BeforeClass(dependsOnMethods = "initCommonFixture")
    public void retrieveApiUrl() {
        Response request = init().baseUri( rootUri.toString() ).accept( JSON ).when().request( GET, "/" );
        JsonPath jsonPath = request.jsonPath();

        this.apiUrl = parseApiUrl( jsonPath );
    }

    /**
     * A.4.2.3. OpenAPI Document Retrieval
     *
     * a) Test Purpose: Validate that the API Definition document can be retrieved from the expected location.
     * 
     * b) Pre-conditions:
     *
     * A URL to the server hosting the API Definition document is known.
     *
     * The test client can authenticate to the server.
     *
     * The test client has sufficient privileges to assess the API Definition document.
     *
     * c) Test Method:
     *
     * Issue an HTTP GET request to the URL {server}/api
     *
     * Validate that a document was returned with a status code 200
     *
     * Validate the contents of the returned document using test A.4.2.4
     *
     * d) References: Requirements 3 and 4
     */
    @Test(description = "Implements A.4.2.3. OpenAPI Document Retrieval (Requirement 3, 4)", groups = "apidefinition", dependsOnGroups = "landingpage")
    public void openapiDocumentRetrieval() {
        if ( apiUrl == null || apiUrl.isEmpty() )
            throw new SkipException( "Api URL could not be parsed from the landing page" );
        Response request = init().baseUri( apiUrl ).accept( JSON ).when().request( GET, "/" );
        request.then().statusCode( 200 );
        response = request.asString();
    }

    /**
     * A.4.2.4. API Definition Validation
     *
     * a) Test Purpose: Validate that the API Definition page complies with the require structure and contents.
     *
     * b) Pre-conditions: The API Definition document has been retrieved from the server
     * 
     * c) Test Method:
     * 
     * Validate the API Definition document against the OpenAPI 3.0 schema
     *
     * Identify the Test Points as described in test A.4.3
     * 
     * Process the API Definition document as described in test A.4.4
     * 
     * d) References: Requirement 4
     *
     * @param testContext
     *            never <code>null</code>
     * @throws MalformedURLException
     *             if the apiUrl is malformed
     */
    @Test(description = "Implements A.4.2.4. API Definition Validation (Requirement 4)", groups = "apidefinition", dependsOnMethods = "openapiDocumentRetrieval")
    public void apiDefinitionValidation( ITestContext testContext )
                            throws MalformedURLException {
        OpenApi3Parser parser = new OpenApi3Parser();

        OpenApi3 apiModel = parser.parse( response, new URL( apiUrl ), true );
        assertTrue( apiModel.isValid(), createValidationMsg( apiModel ) );

        testContext.getSuite().setAttribute( API_MODEL.getName(), apiModel );
    }

    private String parseApiUrl( JsonPath jsonPath ) {
        for ( Object link : jsonPath.getList( "links" ) ) {
            Map<String, Object> linkMap = (Map<String, Object>) link;
            Object rel = linkMap.get( "rel" );
            Object type = linkMap.get( "type" );
            if ( "service".equals( rel ) && OPEN_API_MIME_TYPE.equals( type ) )
                return (String) linkMap.get( "href" );
        }
        return null;
    }

    private String createValidationMsg( OpenApi3 model ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "Landing Page is not valid. Found following validation items:" );
        if ( !model.isValid() ) {
            for ( ValidationResults.ValidationItem item : model.getValidationItems() ) {
                sb.append( "  - " ).append( item.getSeverity() ).append( ": " ).append( item.getMsg() );

            }
        }
        return sb.toString();
    }

}
