package org.opengis.cite.ogcapifeatures10.conformance.crs.discovery.collection;

import java.util.List;

import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.SkipException;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;

/**
 * Verifies object in the paths /collection
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
public class DiscoveryCollectionStorageCrs extends AbstractDiscoveryCollection {

    /**
     * Test: storageCrs property in the collection objects in the path /collections
     *
     * @param collectionId
     *            id of the collection under test, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     */
    @Test(description = "Implements A.1 Discovery, Abstract Test 3 (Requirement /req/crs/fc-md-storageCrs-valid-value), "
                        + "storageCrs property in the collection object in the path /collection", dataProvider = "collectionIdAndJson", dependsOnGroups = "crs-conformance")
    public void verifyCollectionCrsIdentifierOfCrsProperty( String collectionId, JsonPath collection ) {
        String storageCrs = collection.get( "storageCrs" );
        if ( storageCrs == null ) {
            throw new SkipException( String.format( "Collection with id '%s' does not specify a storageCrs",
                                                    collectionId ) );
        }
        List<String> crs = JsonUtils.parseAsList( "crs", collection );
        if ( !crs.contains( storageCrs ) ) {
            throw new AssertionError( String.format( "Collection with id '%s' specifies the storageCrs '%s' which is not declared as crs property",
                                                     collectionId, storageCrs ) );
        }
    }
}
