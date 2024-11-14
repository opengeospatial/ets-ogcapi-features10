package org.opengis.cite.ogcapifeatures10.conformance.crs.discovery.collections;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.testng.ISuite;
import org.testng.ITestContext;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:gabriel.roldan@camptocamp.com">Gabriel Roldan</a>
 */
public class DiscoveryCollectionsStorageCrsTest {
    private static ITestContext testContext;

    private static TestPoint testPoint;

    /**
     * Sample response from a {@code /collections} endpoint. Contains ex
     */
    private static JsonPath collectionsResponse;

    private DiscoveryCollectionsStorageCrs discoveryCollectionsStorageCrs;

    @BeforeClass
    public static void initTestFixture() throws Exception {
        testContext = mock(ITestContext.class);
        ISuite suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);

        testPoint = new TestPoint("http://localhost:8080/api/ogc/features/v1", "/collections", Collections.emptyMap());

        collectionsResponse = prepareCollections();
    }

    @Before
    public void setUp() {
        discoveryCollectionsStorageCrs = new DiscoveryCollectionsStorageCrs();
        discoveryCollectionsStorageCrs.initCommonFixture(testContext);
    }

    /**
     * Test a collection whose crs contains both its {@code storageCrs} and the
     * global {@code #/crs} pointer
     */
    @Test
    public void testVerifyCollectionsPathCollectionCrsPropertyContainsStorageCrs_globalCrsAndStorageCrs() {
        Map<String, Object> collection = collectionsResponse.getMap("collections[0]");
        // preflight checks
        assertNotNull(collection);
        assertEquals("sf:archsites", collection.get("id"));

        @SuppressWarnings("unchecked")
        List<String> crs = (List<String>) collection.get("crs");
        assertThat(collection.get("storageCrs"), equalTo("http://www.opengis.net/def/crs/EPSG/0/26713"));
        assertThat(crs, contains("http://www.opengis.net/def/crs/EPSG/0/26713", "#/crs"));

        // test
        discoveryCollectionsStorageCrs.verifyCollectionsPathCollectionCrsPropertyContainsStorageCrs(testPoint,
                collectionsResponse, collection);
    }

    /**
     * Test a collection whose crs contains only the global {@code #/crs} pointer,
     * and the global crs property contains the collection's storageCrs
     */
    @Test
    public void testVerifyCollectionsPathCollectionCrsPropertyContainsStorageCrs_globalCrs_only() {
        Map<String, Object> collection = collectionsResponse.getMap("collections[1]");
        // preflight checks
        assertNotNull(collection);
        assertEquals("ne:populated_places", collection.get("id"));
        @SuppressWarnings("unchecked")
        List<String> crs = (List<String>) collection.get("crs");
        assertThat(collection.get("storageCrs"), equalTo("http://www.opengis.net/def/crs/OGC/1.3/CRS84"));
        assertThat(crs, contains("#/crs"));

        // test
        discoveryCollectionsStorageCrs.verifyCollectionsPathCollectionCrsPropertyContainsStorageCrs(testPoint,
                collectionsResponse, collection);
    }

    /**
     * Test a collection whose crs contains its {@code storageCrs} and no global
     * {@code #/crs} pointer
     */
    @Test
    public void testVerifyCollectionsPathCollectionCrsPropertyContainsStorageCrs() {
        Map<String, Object> collection = collectionsResponse.getMap("collections[2]");
        // preflight checks
        assertNotNull(collection);
        assertEquals("sf:bugsites", collection.get("id"));
        @SuppressWarnings("unchecked")
        List<String> crs = (List<String>) collection.get("crs");
        assertThat(collection.get("storageCrs"), equalTo("http://www.opengis.net/def/crs/EPSG/0/26713"));
        assertThat(crs, containsInAnyOrder("http://www.opengis.net/def/crs/OGC/1.3/CRS84",
                "http://www.opengis.net/def/crs/EPSG/0/26713"));

        // test
        discoveryCollectionsStorageCrs.verifyCollectionsPathCollectionCrsPropertyContainsStorageCrs(testPoint,
                collectionsResponse, collection);
    }

    /**
     * Test a collection whose storageCrs is declared in the global crs but not on
     * its crs property, but it does contain the global {@code #/crs} pointer.
     */
    @Test
    public void testVerifyCollectionsPathCollectionCrsPropertyContainsStorageCrs_globalCrs_and_custom_crs() {
        Map<String, Object> collection = collectionsResponse.getMap("collections[4]");
        // preflight checks
        assertNotNull(collection);
        assertEquals("storageCrsInGlobalCrs", collection.get("id"));
        @SuppressWarnings("unchecked")
        List<String> crs = (List<String>) collection.get("crs");
        assertThat(collection.get("storageCrs"), equalTo("http://www.opengis.net/def/crs/EPSG/0/4087"));
        assertThat(crs, containsInAnyOrder("#/crs", "http://www.opengis.net/def/crs/EPSG/0/3786"));

        @SuppressWarnings("unchecked")
        List<String> globalCrs = (List<String>) collectionsResponse.get("crs");
        assertTrue(globalCrs.contains("http://www.opengis.net/def/crs/EPSG/0/4087"));

        // AssertionError expected here
        discoveryCollectionsStorageCrs.verifyCollectionsPathCollectionCrsPropertyContainsStorageCrs(testPoint,
                collectionsResponse, collection);
    }

    /**
     * Test a collection whose storageCrs is not declared in its crs and has no
     * pointer to the global crs
     */
    @Test(expected = AssertionError.class)
    public void testVerifyCollectionsPathCollectionCrsPropertyContainsStorageCrs_no_storage_crs() {
        Map<String, Object> collection = collectionsResponse.getMap("collections[5]");
        // preflight checks
        assertNotNull(collection);
        assertEquals("storageCrsUndeclared", collection.get("id"));

        @SuppressWarnings("unchecked")
        List<String> crs = (List<String>) collection.get("crs");
        assertThat(collection.get("storageCrs"), equalTo("http://www.opengis.net/def/crs/EPSG/0/4088"));
        assertThat(crs, not(contains("#/crs")));

        @SuppressWarnings("unchecked")
        List<String> globalCrs = (List<String>) collectionsResponse.get("crs");
        assertFalse(globalCrs.contains("http://www.opengis.net/def/crs/EPSG/0/4088"));

        // AssertionError expected here
        discoveryCollectionsStorageCrs.verifyCollectionsPathCollectionCrsPropertyContainsStorageCrs(testPoint,
                collectionsResponse, collection);
    }

    @Test(expected = AssertionError.class)
    public void testVerifyCollectionsPathCollectionCrsPropertyContainsStorageCrs_no_storage_crs_in_global_crs() {
        Map<String, Object> collection = collectionsResponse.getMap("collections[6]");
        // preflight checks
        assertNotNull(collection);
        assertEquals("storageCrsUndeclaredInGlobalCrs", collection.get("id"));

        @SuppressWarnings("unchecked")
        List<String> crs = (List<String>) collection.get("crs");
        assertThat(collection.get("storageCrs"), equalTo("http://www.opengis.net/def/crs/EPSG/0/4088"));
        assertThat(crs, contains("#/crs"));

        @SuppressWarnings("unchecked")
        List<String> globalCrs = (List<String>) collectionsResponse.get("crs");
        assertFalse(globalCrs.contains("http://www.opengis.net/def/crs/EPSG/0/4088"));

        // AssertionError expected here
        discoveryCollectionsStorageCrs.verifyCollectionsPathCollectionCrsPropertyContainsStorageCrs(testPoint,
                collectionsResponse, collection);
    }

    private static JsonPath prepareCollections() {
        return new JsonPath(Objects
                .requireNonNull(DiscoveryCollectionsStorageCrsTest.class.getResourceAsStream("collections.json")));
    }
}
