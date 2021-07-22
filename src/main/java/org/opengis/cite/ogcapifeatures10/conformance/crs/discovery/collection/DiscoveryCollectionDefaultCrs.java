package org.opengis.cite.ogcapifeatures10.conformance.crs.discovery.collection;

import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertDefaultCrs;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_CODE;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_WITH_HEIGHT_CODE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.hasAtLeastOneSpatialFeatureCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
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

    private Map<String, CoordinateSystem> collectionIdAndDefaultCrs = new HashMap<>();

    @AfterClass
    public void storeCollectionInTestContext( ITestContext testContext ) {
        testContext.getSuite().setAttribute( SuiteAttribute.COLLECTION_DEFAULT_CRS_BY_ID.getName(),
                                             collectionIdAndDefaultCrs );
    }

    /**
     * Test: crs property in the collection objects in the path /collections
     *
     * @param collectionId
     *            id of the collection under test, never <code>null</code>
     * @param collection
     *            the /collection object, never <code>null</code>
     */
    @Test(description = "Implements A.1 Discovery, Abstract Test 2 (Requirement /req/crs/fc-md-crs-list B), "
                        + "crs property contains default crs in the collection object in the path /collection", dataProvider = "collectionIdAndJson", dependsOnGroups = "crs-conformance", groups = "crs-discovery")
    public void verifyCollectionCrsIdentifierOfCrsProperty( String collectionId, JsonPath collection ) {
        Object extent = collection.get( "extent" );
        if ( hasAtLeastOneSpatialFeatureCollection( extent ) ) {
            List<String> crs = JsonUtils.parseAsList( "crs", collection );
            CoordinateSystem defaultCrs = assertDefaultCrs( crs,
                                                            String.format( "Collection with id '%s' does not specify one of the default CRS '%s' or '%s' but provides at least one spatial feature collections",
                                                                           collectionId, DEFAULT_CRS_CODE,
                                                                           DEFAULT_CRS_WITH_HEIGHT_CODE ) );
            collectionIdAndDefaultCrs.put( collectionId, defaultCrs );
        }
    }
}
