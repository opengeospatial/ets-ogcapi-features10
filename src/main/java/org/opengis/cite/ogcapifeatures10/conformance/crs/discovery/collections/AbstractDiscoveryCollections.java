package org.opengis.cite.ogcapifeatures10.conformance.crs.discovery.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.CommonDataFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class AbstractDiscoveryCollections extends CommonDataFixture {

    private Map<TestPoint, JsonPath> collectionsResponses;

    private List<Map<String, Object>> collections;

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.collectionsResponses = (Map<TestPoint, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS_RESPONSE.getName() );
        this.collections = (List<Map<String, Object>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS.getName() );
    }

    @DataProvider(name = "collectionsResponses")
    public Iterator<Object[]> collectionsResponses( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<TestPoint, JsonPath> collectionsResponse : collectionsResponses.entrySet() ) {
            collectionsData.add( new Object[] { collectionsResponse.getKey(), collectionsResponse.getValue() } );
        }
        return collectionsData.iterator();
    }

    @DataProvider(name = "collectionItemUris")
    public Iterator<Object[]> collectionItemUris( ITestContext testContext ) {
        // First test point is used! This may be simplified.
        TestPoint testPoint = collectionsResponses.keySet().stream().findFirst().get();
        JsonPath jsonPath = collectionsResponses.get( testPoint );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            collectionsData.add( new Object[] { testPoint, jsonPath, collection } );
        }
        return collectionsData.iterator();
    }

}
