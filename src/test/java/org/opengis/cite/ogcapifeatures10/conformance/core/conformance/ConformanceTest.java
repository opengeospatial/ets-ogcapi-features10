package org.opengis.cite.ogcapifeatures10.conformance.core.conformance;

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

import org.junit.Test;

import io.restassured.path.json.JsonPath;
import org.opengis.cite.ogcapifeatures10.conformance.RequirementClass;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ConformanceTest {

    @Test
    public void testParseAndValidateRequirementClasses() {
        Conformance conformanceOperationTest = new Conformance();
        InputStream json = ConformanceTest.class.getResourceAsStream( "req-classes.json" );
        JsonPath jsonPath = new JsonPath( json );
        List<RequirementClass> requirementClasses = conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );

        assertThat( requirementClasses.size(), is( 5 ) );
        assertThat( requirementClasses, hasItems( CORE, OPENAPI30, HTML, GEOJSON, GMLSF2 ) );
    }

    @Test(expected = AssertionError.class)
    public void testParseAndValidateRequirementClasses_invalidConformsTo() {
        Conformance conformanceOperationTest = new Conformance();
        InputStream json = ConformanceTest.class.getResourceAsStream( "req-classes_invalidConformsTo.json" );
        JsonPath jsonPath = new JsonPath( json );
        conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );
    }

    @Test(expected = AssertionError.class)
    public void testParseAndValidateRequirementClasses_invalidElementDataType() {
        Conformance conformanceOperationTest = new Conformance();
        InputStream json = ConformanceTest.class.getResourceAsStream( "req-classes_invalidElementDataType.json" );
        JsonPath jsonPath = new JsonPath( json );
        conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );
    }

}