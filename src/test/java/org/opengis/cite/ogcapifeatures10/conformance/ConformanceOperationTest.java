package org.opengis.cite.ogcapifeatures10.conformance;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.CORE;
import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.GEOJSON;
import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.GMLSF2;
import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.HTML;
import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.OPENAPI30;

import java.io.InputStream;
import java.util.List;

import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ConformanceOperationTest {

    @Test
    public void testParseAndValidateRequirementClasses() {
        ConformanceOperation conformanceOperationTest = new ConformanceOperation();
        InputStream json = ConformanceOperationTest.class.getResourceAsStream( "req-classes.json" );
        JsonPath jsonPath = new JsonPath( json );
        List<RequirementClass> requirementClasses = conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );

        assertThat( requirementClasses.size(), is( 5 ) );
        assertThat( requirementClasses, hasItems( CORE, OPENAPI30, HTML, GEOJSON, GMLSF2 ) );
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testParseAndValidateRequirementClasses_invalidConformsTo() {
        ConformanceOperation conformanceOperationTest = new ConformanceOperation();
        InputStream json = ConformanceOperationTest.class.getResourceAsStream( "req-classes_invalidConformsTo.json" );
        JsonPath jsonPath = new JsonPath( json );
        conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testParseAndValidateRequirementClasses_invalidElementDataType() {
        ConformanceOperation conformanceOperationTest = new ConformanceOperation();
        InputStream json = ConformanceOperationTest.class.getResourceAsStream( "req-classes_invalidElementDataType.json" );
        JsonPath jsonPath = new JsonPath( json );
        conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );
    }

}