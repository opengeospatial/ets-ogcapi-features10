package org.opengis.cite.ogcapifeatures10.conformance.crs;

import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.CRS;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.REQUIREMENTCLASSES;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.opengis.cite.ogcapifeatures10.conformance.RequirementClass;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CrsRequirementClassPrecondition {

    private List<RequirementClass> requirementClasses;

    @BeforeClass
    public void requirementClasses( ITestContext testContext ) {
        this.requirementClasses = (List<RequirementClass>) testContext.getSuite().getAttribute( REQUIREMENTCLASSES.getName() );
    }

    /**
     * Verifies that the referenced implements the requirement class
     * http://www.opengis.net/spec/ogcapi-features-2/1.0/req/crs.
     */
    @Test(description = "Precondition: requirement class http://www.opengis.net/spec/ogcapi-features-2/1.0/req/crs must be implemented", groups = "crs-conformance")
    public void verifyRequirementClass() {
        boolean requirementClassIsImplemented = this.requirementClasses != null
                                                && this.requirementClasses.contains( CRS );
        assertTrue( requirementClassIsImplemented, "Requirement class " + CRS.name()
                                                   + " is not supported by the test instance. Tests will be skipped." );
    }

}
