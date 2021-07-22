package org.opengis.cite.ogcapifeatures10.conformance.core.conformance;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.API_MODEL;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.IUT;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.REQUIREMENTCLASSES;
import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.CORE;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForConformance;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.opengis.cite.ogcapifeatures10.conformance.CommonFixture;
import org.opengis.cite.ogcapifeatures10.conformance.RequirementClass;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.opengis.cite.ogcapifeatures10.openapi3.UriBuilder;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * A.2.4. Conformance Path {root}/conformance
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Conformance extends CommonFixture {

    private List<RequirementClass> requirementClasses;

    @DataProvider(name = "conformanceUris")
    public Object[][] conformanceUris( ITestContext testContext ) {
        OpenApi3 apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
        URI iut = (URI) testContext.getSuite().getAttribute( IUT.getName() );
        List<TestPoint> testPoints = retrieveTestPointsForConformance( apiModel, iut );

        //Set dummy TestPoint data if no testPoints found.
        if (testPoints.isEmpty()) {
            testPoints.add(new TestPoint("http://dummydata.com", "/conformance", null));
        }

        Object[][] testPointsData = new Object[testPoints.size()][];
        int i = 0;
        for ( TestPoint testPoint : testPoints ) {
            testPointsData[i++] = new Object[] { testPoint };
        }
        return testPointsData;
    }

    @AfterClass
    public void storeRequirementClassesInTestContext( ITestContext testContext ) {
        testContext.getSuite().setAttribute( REQUIREMENTCLASSES.getName(), this.requirementClasses );
    }

    /**
     * <pre>
     * Abstract Test 7: /ats/core/conformance-op
     * Test Purpose: Validate that a Conformance Declaration can be retrieved from the expected location.
     * Requirement: /req/core/conformance-op
     *
     * Test Method:
     *  1. Construct a path for each "conformance" link on the landing page as well as for the {root}/conformance path.
     *  2. Issue an HTTP GET request on each path
     *  3. Validate that a document was returned with a status code 200
     *  4. Validate the contents of the returned document using test /ats/core/conformance-success.
     * </pre>
     *
     * <pre>
     * Abstract Test 8: /ats/core/conformance-success
     * Test Purpose: Validate that the Conformance Declaration response complies with the required structure and contents.
     * Requirement: /req/core/conformance-success
     *
     * Test Method:
     *  1. Validate the response document against OpenAPI 3.0 schema confClasses.yaml
     *  2. Validate that the document includes the conformance class "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core"
     *  3. Validate that the document list all OGC API conformance classes that the API implements.
     * </pre>
     *
     * @param testPoint
     *            the test point to test, never <code>null</code>
     */
    @Test(description = "Implements A.2.4. Conformance Path {root}/conformance, Abstract Test 7 + 8 (Requirements /req/core/conformance-op) and /req/core/conformance-success", groups = "conformance", dataProvider = "conformanceUris", dependsOnGroups = "apidefinition")
    public void validateConformanceOperationAndResponse( TestPoint testPoint ) {
        String testPointUri = new UriBuilder( testPoint ).buildUrl();

        // Check for dummy data
        if (testPointUri.contains("dummydata")) {
            throw new RuntimeException("No conformance classes found at the /conformance path.");
        }

        Response response = init().baseUri( testPointUri ).accept( JSON ).when().request( GET );
        validateConformanceOperationResponse( testPointUri, response );
    }

    /**
     * Abstract Test 8: /ats/core/conformance-success
     */
    private void validateConformanceOperationResponse( String testPointUri, Response response ) {
        response.then().statusCode( 200 );

        JsonPath jsonPath = response.jsonPath();
        this.requirementClasses = parseAndValidateRequirementClasses( jsonPath );
        assertTrue( this.requirementClasses.contains( CORE ),
                    "Requirement class \"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core\" is not available from path "
                                                              + testPointUri );
    }

    /**
     * @param jsonPath
     *            never <code>null</code>
     * @return the parsed requirement classes, never <code>null</code>
     * @throws AssertionError
     *             if the json does not follow the expected structure
     */
    List<RequirementClass> parseAndValidateRequirementClasses( JsonPath jsonPath ) {
        List<Object> conformsTo = jsonPath.getList( "conformsTo" );
        assertNotNull( conformsTo, "Missing member 'conformsTo'." );

        List<RequirementClass> requirementClasses = new ArrayList<>();
        for ( Object conformTo : conformsTo ) {
            if ( conformTo instanceof String ) {
                String conformanceClass = (String) conformTo;
                RequirementClass requirementClass = RequirementClass.byConformanceClass( conformanceClass );
                if ( requirementClass != null )
                    requirementClasses.add( requirementClass );
            } else
                throw new AssertionError( "At least one element array 'conformsTo' is not a string value (" + conformTo
                                          + ")" );
        }
        return requirementClasses;
    }

}