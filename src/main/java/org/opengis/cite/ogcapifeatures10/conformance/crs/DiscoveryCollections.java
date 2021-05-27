package org.opengis.cite.ogcapifeatures10.conformance.crs;

import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertValidCrsIdentifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.CommonDataFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;

/**
 * Verifies objects in the paths /collections
 * 
 * <pre>
 * Abstract Test 1:  /conf/crs/crs-uri
 * Test Purpose: Verify that each CRS identifier is a valid value
 * Requirement: /req/crs/crs-uri, /req/crs/fc-md-crs-list A, /req/crs/fc-md-storageCrs, /req/crs/fc-md-crs-list-global
 *
 * Test Method
 * For each string value in a crs or storageCrs property in the collections and collection objects in the paths /collections and /collections/{collectionId}, validate that the string conforms to the generic URI syntax as specified by RFC 3986, section 3. In addition, accept a single value of #/crs in each collection object at path /collections, if the collections object has a crs property.
 *  1. For http-URIs (starting with http:) validate that the string conforms to the syntax specified by RFC 7230, section 2.7.1.
 *  2. For https-URIs (starting with https:) validate that the string conforms to the syntax specified by RFC 7230, section 2.7.2.
 *  3. For URNs (starting with urn:) validate that the string conforms to the syntax specified by RFC 8141, section 2.
 *  4. For OGC URNs (starting with urn:ogc:def:crs:) and OGC http-URIs (starting with http://www.opengis.net/def/crs/) validate that the string conforms to the syntax specified by OGC Name Type Specification - definitions - part 1 – basic name.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class DiscoveryCollections extends CommonDataFixture {

    private Map<TestPoint, JsonPath> collectionsResponses;

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.collectionsResponses = (Map<TestPoint, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS_RESPONSE.getName() );
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
        List<Map<String, Object>> collections = (List<Map<String, Object>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            collectionsData.add( new Object[] { testPoint, jsonPath, collection } );
        }
        return collectionsData.iterator();
    }

    /**
     * Test: crs property in the collections object in the path /collections
     * 
     * @param testPoint
     *            test point to test, never <code>null</code>
     * @param jsonPath
     *            the /collections JSON, never <code>null</code>
     */
    @Test(description = "Implements A.1 Discovery, Abstract Test 1 (Requirement /req/crs/crs-uri, /req/crs/fc-md-crs-list A, /req/crs/fc-md-storageCrs, /req/crs/fc-md-crs-list-global), "
                        + "crs property in the collections object in the path /collections", dataProvider = "collectionsResponses", dependsOnGroups = "crs-conformance")
    public void verifyCollectionsCrsIdentifierOfCrsPrperty( TestPoint testPoint, JsonPath jsonPath ) {
        List<String> crs = JsonUtils.parseAsList( "crs", jsonPath );
        for ( String crsValue : crs ) {
            assertValidCrsIdentifier( crsValue,
                                      String.format( "Collections path %s contains invalid CRS identifier property 'crs': '%s'",
                                                     testPoint.getPath(), crsValue ) );
        }
    }

    /**
     * Test: storageCrs property in the collections object in the path /collections
     *
     * @param testPoint
     *            test point to test, never <code>null</code>
     * @param jsonPath
     *            the /collections JSON, never <code>null</code>
     */
    @Test(description = "Implements A.1 Discovery, Abstract Test 1 (Requirement /req/crs/crs-uri, /req/crs/fc-md-crs-list A, /req/crs/fc-md-storageCrs, /req/crs/fc-md-crs-list-global), "
                        + "storageCrs property in the collections object in the path /collections", dataProvider = "collectionsResponses", dependsOnGroups = "crs-conformance")
    public void verifyCollectionsCrsIdentifierOfStorageCrs( TestPoint testPoint, JsonPath jsonPath ) {
        List<String> crs = JsonUtils.parseAsList( "storageCrs", jsonPath );
        for ( String crsValue : crs ) {
            assertValidCrsIdentifier( crsValue,
                                      String.format( "Collections path %s contains invalid CRS identifier property 'storageCrs': '%s'",
                                                     testPoint.getPath(), crsValue ) );
        }
    }

    /**
     * Test: crs property in the collection objects in the path /collections
     *
     * @param testPoint
     *            test point to test, never <code>null</code>
     * @param jsonPath
     *            the /collections JSON, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     */
    @Test(description = "Implements A.1 Discovery, Abstract Test 1 (Requirement /req/crs/crs-uri, /req/crs/fc-md-crs-list A, /req/crs/fc-md-storageCrs, /req/crs/fc-md-crs-list-global), "
                        + "crs property in the collection objects in the path /collections", dataProvider = "collectionItemUris", dependsOnGroups = "crs-conformance")
    public void verifyCollectionsCollectionCrsIdentifierOfCrsProperty( TestPoint testPoint, JsonPath jsonPath,
                                                                       Map<String, Object> collection ) {
        boolean collectionsHasCrsProperty = isCollectionsHasCrsProperty( jsonPath );
        String collectionId = (String) collection.get( "id" );
        List<String> crs = JsonUtils.parseAsList( "crs", collection );
        for ( String crsValue : crs ) {
            isValidCRs( testPoint, collectionsHasCrsProperty, collectionId, crsValue, "crs" );
        }
    }

    /**
     * Test: storageCrs property in the collection objects in the path /collections
     *
     * @param testPoint
     *            test point to test, never <code>null</code>
     * @param jsonPath
     *            the /collections JSON, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     */
    @Test(description = "Implements A.1 Discovery, Abstract Test 1 (Requirement /req/crs/crs-uri, /req/crs/fc-md-crs-list A, /req/crs/fc-md-storageCrs, /req/crs/fc-md-crs-list-global), "
                        + "storageCrs property in the collection objects in the path /collections", dataProvider = "collectionItemUris", dependsOnGroups = "crs-conformance")
    public void verifyCollectionsCollectionCrsIdentifierOfStorageCrs( TestPoint testPoint, JsonPath jsonPath,
                                                                      Map<String, Object> collection ) {
        boolean collectionsHasCrsProperty = isCollectionsHasCrsProperty( jsonPath );
        String collectionId = (String) collection.get( "id" );
        List<String> crs = JsonUtils.parseAsList( "storageCrs", collection );
        for ( String crsValue : crs ) {
            isValidCRs( testPoint, collectionsHasCrsProperty, collectionId, crsValue, "storageCrs" );
        }
    }

    private boolean isCollectionsHasCrsProperty( JsonPath jsonPath ) {
        List<String> collectionsCrs = jsonPath.getList( "crs" );
        return collectionsCrs != null && !collectionsCrs.isEmpty();
    }

    private void isValidCRs( TestPoint testPoint, boolean collectionsHasCrsProperty, String collectionId,
                             String crsValue, String propertyName ) {
        if ( "#/crs".equals( crsValue ) && collectionsHasCrsProperty ) {
            return;
        }
        assertValidCrsIdentifier( crsValue,
                                  String.format( "Collection with id '%s' at collections path %s contains invalid CRS identifier property '%s': '%s'",
                                                 collectionId, testPoint.getPath(), propertyName, crsValue ) );
    }

}
