package org.opengis.cite.ogcapifeatures10.conformance.crs.discovery.collections;

import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.SkipException;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;

/**
 * Verifies objects in the paths /collections
 *
 * <pre>
 * Abstract Test 3: /conf/crs/storageCrs Test Purpose: Verify that the storage CRS identifier is a valid value
 * Requirement: /req/crs/fc-md-storageCrs-valid-value
 *
 * Test Method: For each collection object that includes a storageCrs property in the paths /collections and
 * /collections/{collectionId}, validate that the string is also found in the crs property of the collection or, in case
 * the crs property includes a value #/crs, in the global list of CRSs.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class DiscoveryCollectionsStorageCrs extends AbstractDiscoveryCollections {

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
    @Test(description = "Implements A.1 Discovery, Abstract Test 2 (Requirement /req/crs/fc-md-storageCrs-valid-value), "
                        + "storageCrs property in the collection objects in the path /collections", dataProvider = "collectionItemUris", dependsOnGroups = "crs-conformance")
    public void verifyCollectionsPathCollectionCrsPropertyContainsDefaultCrs( TestPoint testPoint, JsonPath jsonPath,
                                                                              Map<String, Object> collection ) {
        String collectionId = (String) collection.get( "id" );
        String storageCrs = (String) collection.get( "storageCrs" );
        if ( storageCrs == null ) {
            throw new SkipException( String.format( "Collection with id '%s' at collections path %s does not specify a storageCrs",
                                                    collectionId, testPoint.getPath() ) );
        }
        List<String> crs = JsonUtils.parseAsList( "crs", collection );
        if ( crs.size() == 1 && "#/crs".equals( crs.get( 0 ) ) ) {
            List<String> globalCrsList = JsonUtils.parseAsList( "crs", jsonPath );
            if ( !globalCrsList.contains( storageCrs ) ) {
                throw new AssertionError( String.format( "Collection with id '%s' at collections path %s specifies the storageCrs '%s' which is not declared in the global list of CRSs",
                                                         collectionId, testPoint.getPath(), storageCrs ) );
            }
        } else if ( !crs.contains( storageCrs ) ) {
            throw new AssertionError( String.format( "Collection with id '%s' at collections path %s specifies the storageCrs '%s' which is not declared as crs property",
                                                     collectionId, testPoint.getPath(), storageCrs ) );
        }
    }

}