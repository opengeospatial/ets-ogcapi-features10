package org.opengis.cite.wfs30.conformance;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.wfs30.SuiteAttribute.API_MODEL;
import static org.opengis.cite.wfs30.WFS3.PATH.CONFORMANCE;
import static org.opengis.cite.wfs30.openapi3.OpenApiUtils.retrieveTestPoints;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.opengis.cite.wfs30.CommonFixture;
import org.opengis.cite.wfs30.openapi3.TestPoint;
import org.opengis.cite.wfs30.openapi3.UriBuilder;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ConformanceOperation extends CommonFixture {

    @DataProvider(name = "conformanceUris")
    public Object[][] conformanceUris( ITestContext testContext ) {
        OpenApi3 apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
        List<TestPoint> testPoints = retrieveTestPoints( apiModel, CONFORMANCE );
        return new Object[][] { testPoints.toArray() };
    }

    /**
     * Implements A.4.4.2. Validate Conformance Operation and A.4.4.3. Validate Conformance Operation Response.
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "Implements A.4.4.2. Validate Conformance Operation (Requirement 5) and A.4.4.3. Validate Conformance Operation Response (Requirement 6)", dataProvider = "conformanceUris", dependsOnGroups = "apidefinition")
    public void validateConformanceOperationAndResponse( TestPoint testPoint ) {
        Response response = validateConformanceOperation( testPoint );
        validateConformanceOperationResponse( testPoint, response );
    }

    /**
     * A.4.4.2. Validate Conformance Operation
     *
     * a) Test Purpose: Validate that Conformance Operation behaves as required.
     *
     * b) Pre-conditions: Path = /conformance
     *
     * c) Test Method:
     *
     * DO FOR each /conformance test point
     *
     * Issue an HTTP GET request using the test point URI
     *
     * Go to test A.4.4.3.
     *
     * d) References: Requirement 5
     */
    private Response validateConformanceOperation( TestPoint testPoint ) {
        String testPointUri = new UriBuilder( testPoint ).buildUrl();
        return init().baseUri( testPointUri ).accept( JSON ).when().request( GET );
    }

    /**
     * A.4.4.3. Validate Conformance Operation Response
     *
     * a) Test Purpose: Validate the Response to the Conformance Operation.
     *
     * b) Pre-conditions:
     *
     * Path = /conformance
     *
     * A Conformance document has been retrieved
     *
     * c) Test Method:
     *
     * Validate the retrieved document against the classes.yaml schema.
     *
     * Record all reported compliance classes and associate that list with the test point. This information will be used
     * in latter tests.
     *
     * d) References: Requirement 6
     */
    private void validateConformanceOperationResponse( TestPoint testPoint, Response response ) {
        response.then().statusCode( 200 );

        JsonPath jsonPath = response.jsonPath();
        List<String> requirementClasses = parseAndValidateRequirementClasses( jsonPath );
        testPoint.addRequirementClasses( requirementClasses );
    }

    /**
     * @param jsonPath
     *            never <code>null</code>
     * @return the parsed requirement classes, never <code>null</code>
     * @throws AssertionError
     *             if the json does not follow the expected structure
     */
    List<String> parseAndValidateRequirementClasses( JsonPath jsonPath ) {
        List<String> requirementClasses = new ArrayList<>();
        List<Object> conformsTo = jsonPath.getList( "conformsTo" );
        assertNotNull( conformsTo, "Missing member 'conformsTo'." );

        for ( Object conformTo : conformsTo ) {
            if ( conformTo instanceof String )
                requirementClasses.add( (String) conformTo );
            else
                throw new AssertionError( "At least one element array 'conformsTo' is not a string value (" + conformTo
                                          + ")" );
        }
        return requirementClasses;
    }

}