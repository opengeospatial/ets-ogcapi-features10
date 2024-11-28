package org.opengis.cite.ogcapifeatures10.conformance.crs.discovery.collections;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
public class DiscoveryCollectionsDefaultCrsTest {

	private static ITestContext testContext;

	private static TestPoint testPoint;

	/**
	 * Sample response from a {@code /collections} endpoint. Contains ex
	 */
	private static JsonPath collectionsResponse;

	private DiscoveryCollectionsDefaultCrs discoveryCollectionsDefaultCrs;

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
		discoveryCollectionsDefaultCrs = new DiscoveryCollectionsDefaultCrs();
		discoveryCollectionsDefaultCrs.initCommonFixture(testContext);
	}

	/**
	 * Test a collection whose crs contains both its {@code storageCrs} and the global
	 * {@code #/crs} pointer
	 */
	@Test
	public void testVerifyCollectionsPathCollectionCrsPropertyContainsDefaultCrs_globalCrsAndStorageCrs() {
		Map<String, Object> collection = collectionsResponse.getMap("collections[0]");
		// preflight checks
		assertNotNull(collection);
		assertEquals("sf:archsites", collection.get("id"));
		@SuppressWarnings("unchecked")
		List<String> crs = (List<String>) collection.get("crs");
		assertThat(collection.get("storageCrs"), equalTo("http://www.opengis.net/def/crs/EPSG/0/26713"));
		assertThat(crs, containsInAnyOrder("#/crs", "http://www.opengis.net/def/crs/EPSG/0/26713"));

		// test
		discoveryCollectionsDefaultCrs.verifyCollectionsPathCollectionCrsPropertyContainsDefaultCrs(testPoint,
				collectionsResponse, collection);
	}

	/**
	 * Test a collection whose crs contains only the global {@code #/crs} pointer
	 */
	@Test
	public void testVerifyCollectionsPathCollectionCrsPropertyContainsDefaultCrs_globalCrs() {
		Map<String, Object> collection = collectionsResponse.getMap("collections[1]");
		// preflight checks
		assertNotNull(collection);
		assertEquals("ne:populated_places", collection.get("id"));
		@SuppressWarnings("unchecked")
		List<String> crs = (List<String>) collection.get("crs");
		assertThat(collection.get("storageCrs"), equalTo("http://www.opengis.net/def/crs/OGC/1.3/CRS84"));
		assertThat(crs, contains("#/crs"));

		// test
		discoveryCollectionsDefaultCrs.verifyCollectionsPathCollectionCrsPropertyContainsDefaultCrs(testPoint,
				collectionsResponse, collection);
	}

	/**
	 * Test a collection whose crs contains a list of crs identifiers without the global
	 * {@code #/crs} pointer
	 */
	@Test
	public void testVerifyCollectionsPathCollectionCrsPropertyContainsDefaultCrs() {
		Map<String, Object> collection = collectionsResponse.getMap("collections[2]");
		// preflight checks
		assertNotNull(collection);
		assertEquals("sf:bugsites", collection.get("id"));
		@SuppressWarnings("unchecked")
		List<String> crs = (List<String>) collection.get("crs");
		assertThat(collection.get("storageCrs"), equalTo("http://www.opengis.net/def/crs/EPSG/0/26713"));
		assertThat(crs, contains("http://www.opengis.net/def/crs/EPSG/0/26713",
				"http://www.opengis.net/def/crs/OGC/1.3/CRS84"));

		// test
		discoveryCollectionsDefaultCrs.verifyCollectionsPathCollectionCrsPropertyContainsDefaultCrs(testPoint,
				collectionsResponse, collection);
	}

	/**
	 * Test a collection whose crs does not declare any of the default CRS nor has the
	 * global {@code #/crs} pointer
	 */
	@Test(expected = AssertionError.class)
	public void testVerifyCollectionsPathCollectionCrsPropertyContainsDefaultCrs_no_default_crs() {
		Map<String, Object> collection = collectionsResponse.getMap("collections[3]");
		// preflight checks
		assertNotNull(collection);
		assertEquals("no_default_crs", collection.get("id"));

		// AssertionError expected here
		discoveryCollectionsDefaultCrs.verifyCollectionsPathCollectionCrsPropertyContainsDefaultCrs(testPoint,
				collectionsResponse, collection);
	}

	private static JsonPath prepareCollections() {
		return new JsonPath(Objects
			.requireNonNull(DiscoveryCollectionsDefaultCrsTest.class.getResourceAsStream("collections.json")));
	}

}
