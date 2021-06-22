package org.opengis.cite.ogcapifeatures10.conformance.crs.discovery.collection;

import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertValidCrsIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;

/**
 * Verifies object in the paths /collection
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
public class DiscoveryCollectionCrsUri {

    private Map<String, List<CoordinateSystem>> collectionIdAndValidCrs = new HashMap<>();

    @DataProvider(name = "collectionIdAndJsonAndCrs")
    public Iterator<Object[]> collectionIdAndJsonAndCrs( ITestContext testContext ) {
        Map<String, JsonPath> collectionsResponses = (Map<String, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_BY_ID.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            List<CoordinateSystem> crs = parseCrs( collection.getValue() );
            for ( CoordinateSystem coordinateSystem : crs ) {
                collectionsData.add( new Object[] { collection.getKey(), coordinateSystem } );
            }
        }
        return collectionsData.iterator();
    }

    @DataProvider(name = "collectionIdAndJsonAndStorageCrs")
    public Iterator<Object[]> collectionIdAndJsonAndStorageCrs( ITestContext testContext ) {
        Map<String, JsonPath> collectionsResponses = (Map<String, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_BY_ID.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            CoordinateSystem storageCrs = parseStorageCrs( collection.getValue() );
            if ( storageCrs != null )
                collectionsData.add( new Object[] { collection.getKey(), storageCrs } );
        }
        return collectionsData.iterator();
    }

    @AfterClass
    public void storeCollectionInTestContext( ITestContext testContext ) {
        testContext.getSuite().setAttribute( SuiteAttribute.COLLECTION_CRS_BY_ID.getName(), collectionIdAndValidCrs );
    }

    /**
     * Test: crs property in the collection objects in the path /collections
     *
     * @param collectionId
     *            id of the collection under test, never <code>null</code>
     * @param crs
     *            the coordinate system, never <code>null</code>
     */
    @Test(description = "Implements A.1 Discovery, Abstract Test 1 (Requirement /req/crs/crs-uri, /req/crs/fc-md-crs-list A, /req/crs/fc-md-storageCrs, /req/crs/fc-md-crs-list-global), "
                        + "crs property in the collection object in the path /collection", dataProvider = "collectionIdAndJsonAndCrs", dependsOnGroups = "crs-conformance", groups = "crs-discovery")
    public void verifyCollectionCrsIdentifierOfCrsProperty( String collectionId, CoordinateSystem crs ) {
        addCrs( collectionId, crs );
        assertValidCrsIdentifier( crs,
                                  String.format( "Collection with id '%s' contains invalid CRS identifier property 'crs': '%s'",
                                                 collectionId, crs ) );
    }

    /**
     * Test: storageCrs property in the collection objects in the path /collections
     *
     * @param collectionId
     *            id of the collection under test, never <code>null</code>
     * @param storageCrs
     *            the storageCrs, never <code>null</code>
     */
    @Test(description = "Implements A.1 Discovery, Abstract Test 1 (Requirement /req/crs/crs-uri, /req/crs/fc-md-crs-list A, /req/crs/fc-md-storageCrs, /req/crs/fc-md-crs-list-global), "
                        + "storageCrs property in the collection object in the path /collection", dataProvider = "collectionIdAndJsonAndStorageCrs", dependsOnGroups = "crs-conformance", groups = "crs-discovery")
    public void verifyCollectionCrsIdentifierOfStorageCrsProperty( String collectionId, CoordinateSystem storageCrs ) {
        assertValidCrsIdentifier( storageCrs,
                                  String.format( "Collection with id '%s' contains invalid CRS identifier property 'storageCrs': '%s'",
                                                 collectionId, storageCrs ) );
    }

    private List<CoordinateSystem> parseCrs( JsonPath collection ) {
        return JsonUtils.parseAsList( "crs",
                                      collection ).stream().map( crsValue -> new CoordinateSystem( crsValue ) ).collect( Collectors.toList() );
    }

    private CoordinateSystem parseStorageCrs( JsonPath collection ) {
        String storageCrs = JsonUtils.parseAsString( collection.get( "storageCrs" ) );
        if ( storageCrs == null )
            return null;
        return new CoordinateSystem( storageCrs );
    }

    private void addCrs( String collectionId, CoordinateSystem coordinateSystem ) {
        collectionIdAndValidCrs.putIfAbsent( collectionId, new ArrayList<>() );
        collectionIdAndValidCrs.get( collectionId ).add( coordinateSystem );
    }
}
