package org.opengis.cite.ogcapifeatures10.conformance.crs.discovery;

import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertDefaultCrs;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_WITH_HEIGHT;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.hasAtLeastOneSpatialFeatureCollection;

import java.util.List;

import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;

/**
 * Verifies object in the paths /collection
 *
 * <pre>
 * Abstract Test 2: /conf/crs/default-crs
 * Test Purpose: Verify that the list of supported CRSs includes the default CRS.
 * Requirement: /req/crs/fc-md-crs-list B
 *
 * Test Method
 * For each string value in a crs property in a collection object (for each path /collections and /collections/{collectionId})
 * validate that either
 * http://www.opengis.net/def/crs/OGC/1.3/CRS84 or
 * http://www.opengis.net/def/crs/OGC/1.3/CRS84h
 * is included in the array, if the collection has a spatial extent, i.e., is a spatial feature collection.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class DiscoveryCollectionDefaultCrs extends AbstractDiscoveryCollection {

    /**
     * Test: crs property in the collection objects in the path /collections
     *
     * @param collectionId
     *            id of the collection under test, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     */
    @Test(description = "Implements A.1 Discovery, Abstract Test 1 (Requirement /req/crs/crs-uri, /req/crs/fc-md-crs-list A, /req/crs/fc-md-storageCrs, /req/crs/fc-md-crs-list-global), "
                        + "crs property contains default crs in the collection object in the path /collection", dataProvider = "collectionIdAndJson", dependsOnGroups = "crs-conformance")
    public void verifyCollectionCrsIdentifierOfCrsProperty( String collectionId, JsonPath collection ) {
        Object extent = collection.get( "extent" );
        if ( hasAtLeastOneSpatialFeatureCollection( extent ) ) {
            List<String> crs = JsonUtils.parseAsList( "crs", collection );
            assertDefaultCrs( crs,
                              String.format( "Collection with id '%s' does not specify one of the default CRS '%s' or '%s' but provides at least one spatial feature collections",
                                             collectionId, DEFAULT_CRS, DEFAULT_CRS_WITH_HEIGHT ) );
        }
    }
}