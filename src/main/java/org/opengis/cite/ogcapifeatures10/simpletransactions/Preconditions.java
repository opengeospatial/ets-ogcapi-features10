package org.opengis.cite.ogcapifeatures10.simpletransactions;

import static org.opengis.cite.ogcapifeatures10.SuiteAttribute.REQUIREMENTCLASSES;
import static org.opengis.cite.ogcapifeatures10.conformance.RequirementClass.SIMPLETRANSACTIONS;

import java.util.List;

import org.opengis.cite.ogcapifeatures10.CommonFixture;
import org.opengis.cite.ogcapifeatures10.conformance.RequirementClass;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * Preconditions
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 */
public class Preconditions extends CommonFixture {

    /**
     * 
     * Checks if 'Simple Transactions' conformance class is supported.
     * 
     * @param testContext
     *            test context
     */
    @Test(description = "Checks if 'Simple Transactions' conformance class is supported")
    public void checkIfSimpleTransactionsIsSupported( ITestContext testContext ) {
        List<RequirementClass> requirementClasses = (List<RequirementClass>) testContext.getSuite().getAttribute( REQUIREMENTCLASSES.getName() );
        if ( requirementClasses == null || !requirementClasses.contains( SIMPLETRANSACTIONS ) )
            throw new SkipException( "'Simple Transactions' conformance class is not supported" );
    }

}