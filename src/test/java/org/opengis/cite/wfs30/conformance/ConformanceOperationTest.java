package org.opengis.cite.wfs30.conformance;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        List<String> requirementClasses = conformanceOperationTest.parseAndValidateRequirementClasses( jsonPath );

        assertThat( requirementClasses.size(), is( 5 ) );
        assertThat( requirementClasses, hasItem( "http://www.opengis.net/spec/wfs-1/3.0/req/core" ) );
        assertThat( requirementClasses, hasItem( "http://www.opengis.net/spec/wfs-1/3.0/req/oas30" ) );
        assertThat( requirementClasses, hasItem( "http://www.opengis.net/spec/wfs-1/3.0/req/html" ) );
        assertThat( requirementClasses, hasItem( "http://www.opengis.net/spec/wfs-1/3.0/req/geojson" ) );
        assertThat( requirementClasses, hasItem( "http://www.opengis.net/spec/wfs-1/3.0/req/gmlsf2" ) );
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