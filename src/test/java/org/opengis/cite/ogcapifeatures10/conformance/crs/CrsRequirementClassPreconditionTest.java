package org.opengis.cite.ogcapifeatures10.conformance.crs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.CORE;
import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.OPENAPI30;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.conformance.RequirementClass;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CrsRequirementClassPreconditionTest {

    @Test
    public void verifyRequirementClass_ImplementsCrs() {
        CrsRequirementClassPrecondition requirementClassPrecondition = new CrsRequirementClassPrecondition();
        List<RequirementClass> allRequirementClasses = Arrays.asList( RequirementClass.values() );
        requirementClassPrecondition.requirementClasses( mockTestContext( allRequirementClasses ) );
        requirementClassPrecondition.verifyRequirementClass();
    }

    @Test(expected = AssertionError.class)
    public void verifyRequirementClass_NotImplementsCrs() {
        CrsRequirementClassPrecondition requirementClassPrecondition = new CrsRequirementClassPrecondition();
        List<RequirementClass> requirementClasses = Arrays.asList( CORE, OPENAPI30 );
        requirementClassPrecondition.requirementClasses( mockTestContext( requirementClasses ) );
        requirementClassPrecondition.verifyRequirementClass();
    }

    @Test(expected = AssertionError.class)
    public void verifyRequirementClass_Null() {
        CrsRequirementClassPrecondition requirementClassPrecondition = new CrsRequirementClassPrecondition();
        requirementClassPrecondition.requirementClasses( mockTestContext( null ) );
        requirementClassPrecondition.verifyRequirementClass();
    }

    @Test(expected = AssertionError.class)
    public void verifyRequirementClass_Empty() {
        CrsRequirementClassPrecondition requirementClassPrecondition = new CrsRequirementClassPrecondition();
        List<RequirementClass> requirementClasses = Collections.emptyList();
        requirementClassPrecondition.requirementClasses( mockTestContext( requirementClasses ) );
        requirementClassPrecondition.verifyRequirementClass();
    }

    private ITestContext mockTestContext( List<RequirementClass> requirementClasses ) {
        ITestContext testContext = mock( ITestContext.class );
        ISuite suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        when( suite.getAttribute( SuiteAttribute.REQUIREMENTCLASSES.getName() ) ).thenReturn( requirementClasses );
        return testContext;
    }
}
