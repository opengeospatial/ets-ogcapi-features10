package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Method.GET;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertDefaultCrs;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertDefaultCrsAtFirst;
import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_CODE;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.DEFAULT_CRS_WITH_HEIGHT_CODE;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.IUT;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollectionsMetadata;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinkByRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinksByRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findSupportedEncodingLinksWithoutRel;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.findUnsupportedTypes;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.linkIncludesRelAndType;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.linkIncludesRelAndHref;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseAsListOfMaps;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseAsString;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.opengis.cite.ogcapifeatures10.EtsAssert;
import org.opengis.cite.ogcapifeatures10.OgcApiFeatures10;
import org.opengis.cite.ogcapifeatures10.conformance.CommonDataFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.opengis.cite.ogcapifeatures10.openapi3.UriBuilder;
import org.opengis.cite.ogcapifeatures10.util.ClientUtils;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.opengis.cite.ogcapifeatures10.util.TestSuiteLogger;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.path.json.config.JsonPathConfig.NumberReturnType;
import io.restassured.response.Response;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.Invocation.Builder;

/**
 * A.2.5. Feature Collections {root}/collections
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureCollections extends CommonDataFixture {

	private final Map<TestPoint, Response> testPointAndResponses = new HashMap<>();

	private final Map<TestPoint, List<Map<String, Object>>> testPointAndCollections = new HashMap<>();

	private Object[][] testPointsData;

	/**
	 * <p>
	 * collectionsUris.
	 * </p>
	 * @param testContext a {@link org.testng.ITestContext} object
	 * @return an array of {@link java.lang.Object} objects
	 */
	@DataProvider(name = "collectionsUris")
	public Object[][] collectionsUris(ITestContext testContext) {
		if (this.testPointsData == null) {
			URI iut = (URI) testContext.getSuite().getAttribute(IUT.getName());
			List<TestPoint> testPoints = retrieveTestPointsForCollectionsMetadata(getApiModel(), iut);
			this.testPointsData = new Object[testPoints.size()][];
			int i = 0;
			for (TestPoint testPoint : testPoints) {
				this.testPointsData[i++] = new Object[] { testPoint };
			}
		}
		return testPointsData;
	}

	/**
	 * <p>
	 * storeCollectionsInTestContext.
	 * </p>
	 * @param testContext a {@link org.testng.ITestContext} object
	 */
	@AfterClass
	public void storeCollectionsInTestContext(ITestContext testContext) {
		List<Map<String, Object>> collections = new ArrayList<>();
		for (List<Map<String, Object>> testPointAndCollection : testPointAndCollections.values()) {
			collections.addAll(testPointAndCollection);
		}
		testContext.getSuite().setAttribute(SuiteAttribute.COLLECTIONS.getName(), collections);
	}

	/**
	 * <p>
	 * storeCollectionsResponseInTestContext.
	 * </p>
	 * @param testContext a {@link org.testng.ITestContext} object
	 */
	@AfterClass
	public void storeCollectionsResponseInTestContext(ITestContext testContext) {
		Map<TestPoint, JsonPath> collectionsResponses = new HashMap<>();
		for (Map.Entry<TestPoint, Response> testPointAndResponse : testPointAndResponses.entrySet()) {
			if (testPointAndResponse.getValue() != null) {
				JsonPath jsonPath = testPointAndResponse.getValue().jsonPath();
				collectionsResponses.put(testPointAndResponse.getKey(), jsonPath);
			}
		}
		testContext.getSuite().setAttribute(SuiteAttribute.COLLECTIONS_RESPONSE.getName(), collectionsResponses);
	}

	/**
	 * <pre>
	 * Abstract Test 9: /ats/core/fc-md-op (v1.0.0), /conf/core/fc-md-op (v1.0.1)
	 * Test Purpose:  Validate that information about the Collections can be retrieved from the expected location.
	 * Requirement: /req/core/fc-md-op
	 *
	 * Test Method
	 *  1. Issue an HTTP GET request to the URL {root}/collections
	 *  2. Validate that a document was returned with a status code 200
	 *  3. Validate the contents of the returned document using test /ats/core/fc-md-success (v1.0.0), /conf/core/fc-md-success (v1.0.1).
	 * </pre>
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "Implements A.2.5. Feature Collections {root}/collections, Abstract Test 9 (Requirement /req/core/fc-md-op)",
			groups = "collections", dataProvider = "collectionsUris",
			dependsOnGroups = { "apidefinition", "conformance" })
	public void validateFeatureCollectionsMetadataOperation(TestPoint testPoint) {
		String testPointUri = new UriBuilder(testPoint).buildUrl();
		Response response = init().baseUri(testPointUri).accept(JSON).when().request(GET);
		response.then().statusCode(200);
		this.testPointAndResponses.put(testPoint, response);
	}

	/**
	 * Abstract Test 10, Test Method 1
	 *
	 * <pre>
	 * Abstract Test 10: /ats/core/fc-md-success (v1.0.0), /conf/core/fc-md-success (v1.0.1)
	 * Test Purpose: Validate that the Collections content complies with the required structure and contents.
	 * Requirement: /req/core/fc-md-success, /req/core/crs84
	 *
	 * Test Method
	 *  1. Validate that all response documents comply with /ats/core/fc-md-links (v1.0.0), /conf/core/fc-md-links (v1.0.1)
	 * </pre>
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 10, Test Method 1 (Requirement /req/core/fc-md-success, /req/core/crs84)",
			groups = "collections", dataProvider = "collectionsUris",
			dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
	public void validateFeatureCollectionsMetadataOperationResponse_Links(TestPoint testPoint) {
		Response response = testPointAndResponses.get(testPoint);
		if (response == null)
			throw new SkipException("Could not find a response for test point " + testPoint);

		JsonPath jsonPath = response.jsonPath();
		List<Map<String, Object>> links = parseAsListOfMaps("links", jsonPath);

		// Requirement 13 A (1): a link to this response document (relation: self)
		Map<String, Object> linkToSelf = findLinkByRel(links, "self");
		assertNotNull(linkToSelf, "Feature Collections Metadata document must include a link for itself");
		// Requirement 13B: All links SHALL include the rel and type link parameters.
		assertTrue(linkIncludesRelAndType(linkToSelf), "Link to itself must include a rel and type parameter");

		// Requirement 13 A (2): a link to the response document in every other media
		// type
		// supported by the server
		// (relation: alternate)
		// Dev: Supported media type are identified by the compliance classes for this
		// server
		List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForOtherResources(linkToSelf);
		List<Map<String, Object>> alternateLinks = findLinksWithSupportedMediaTypeByRel(links, mediaTypesToSupport,
				"alternate");
		List<String> typesWithoutLink = findUnsupportedTypes(alternateLinks, mediaTypesToSupport);
		assertTrue(typesWithoutLink.isEmpty(),
				"Feature Collections Metadata document must include links for alternate encodings. Missing links for types "
						+ typesWithoutLink);

		// Requirement 13 B: All "self"/"alternate" links SHALL include the rel and type
		// link parameters.
		Set<String> rels = new HashSet<>();
		rels.add("self");
		rels.add("alternate");
		List<String> linksWithoutRelOrType = findLinksWithoutRelOrType(alternateLinks, rels);
		assertTrue(linksWithoutRelOrType.isEmpty(),
				"Links for alternate encodings must include a rel and type parameter. Missing for links "
						+ linksWithoutRelOrType);
	}

	/**
	 * Abstract Test 10, Test Method 2
	 *
	 * <pre>
	 * Abstract Test 10: /ats/core/fc-md-success (v1.0.0), /conf/core/fc-md-success (v1.0.1)
	 * Test Purpose: Validate that the Collections content complies with the required structure and contents.
	 * Requirement: /req/core/fc-md-success, /req/core/crs84
	 *
	 * Test Method
	 *  2. Validate that all response documents comply with /ats/core/fc-md-items (v1.0.0), /conf/core/fc-md-items (v1.0.1)
	 * </pre>
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 10, Test Method 2 (Requirement /req/core/fc-md-success, /req/core/crs84)",
			groups = "collections", dataProvider = "collectionsUris",
			dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
	public void validateFeatureCollectionsMetadataOperationResponse_Items(TestPoint testPoint) {
		Response response = testPointAndResponses.get(testPoint);
		if (response == null)
			throw new SkipException("Could not find a response for test point " + testPoint);
		JsonPathConfig config = JsonPathConfig.jsonPathConfig().numberReturnType(NumberReturnType.DOUBLE);
		JsonPath jsonPath = response.jsonPath(config);
		List<Object> collections = jsonPath.getList("collections");

		// Test method cannot be verified as the provided collections are not known.

		this.testPointAndCollections.put(testPoint, createCollectionsMap(collections));
	}

	/**
	 * Abstract Test 10, Test Method 3
	 *
	 * <pre>
	 * Abstract Test 10: /ats/core/fc-md-success (v1.0.0), /conf/core/fc-md-success (v1.0.1)
	 * Test Purpose: Validate that the Collections content complies with the required structure and contents.
	 * Requirement: /req/core/fc-md-success, /req/core/crs84
	 *
	 * Test Method
	 *  3. In case the response includes a "crs" property, validate that the first value is either "http://www.opengis.net/def/crs/OGC/1.3/CRS84" or "http://www.opengis.net/def/crs/OGC/0/CRS84h"
	 * </pre>
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 10, Test Method 3 (Requirement /req/core/fc-md-success, /req/core/crs84)",
			groups = "collections", dataProvider = "collectionsUris",
			dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
	public void validateFeatureCollectionsMetadataOperationResponse_CrsProperty(TestPoint testPoint) {
		Response response = testPointAndResponses.get(testPoint);
		if (response == null)
			throw new SkipException("Could not find a response for test point " + testPoint);
		JsonPath jsonPath = response.jsonPath();
		if (jsonPath.get("crs") != null) {
			List<String> crs = JsonUtils.parseAsList("crs", jsonPath);
			assertDefaultCrsAtFirst(crs, String.format(
					"Feature Collections Metadata document does not specify one of the default CRS '%s' or '%s' as first value.",
					DEFAULT_CRS_CODE, DEFAULT_CRS_WITH_HEIGHT_CODE));
		}
	}

	/**
	 * Abstract Test 10, Test Method 4
	 *
	 * <pre>
	 * Abstract Test 10: /ats/core/fc-md-success (v1.0.0), /conf/core/fc-md-success (v1.0.1)
	 * Test Purpose: Validate that the Collections content complies with the required structure and contents.
	 * Requirement: /req/core/fc-md-success, /req/core/crs84
	 *
	 * Test Method
	 *  4. Validate the collections content for all supported media types using the resources and tests identified in Schema and Tests for Collections content
	 * </pre>
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 10, Test Method 4 (Requirement /req/core/fc-md-success, /req/core/crs84)",
			groups = "collections", dataProvider = "collectionsUris",
			dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
	public void validateFeatureCollectionsMetadataOperationResponse_Content(TestPoint testPoint) {
		Response response = testPointAndResponses.get(testPoint);
		if (response == null)
			throw new SkipException("Could not find a response for test point " + testPoint);

		JsonPath jsonPath = response.jsonPath();
		List<Map<String, Object>> links = parseAsListOfMaps("links", jsonPath);
		boolean linksAreAvailable = links != null && !links.isEmpty();
		assertTrue(linksAreAvailable, "Feature Collections Metadata document does not contain links.");

		List<Map<String, Object>> collections = parseAsListOfMaps("collections", jsonPath);
		boolean collectionsAreAvailable = collections != null && !collections.isEmpty();
		assertTrue(collectionsAreAvailable, "Feature Collections Metadata document does not contain collections.");
	}

	private List<Map<String, Object>> createCollectionsMap(List<Object> collections) {
		List<Map<String, Object>> collectionsMap = new ArrayList<>();
		for (Object collectionObj : collections) {
			Map<String, Object> collection = (Map<String, Object>) collectionObj;
			if (null != collection.get("id")) {
				String itemType = (String) collection.get("itemType");
				if (StringUtils.isEmpty(itemType) || itemType.equalsIgnoreCase("feature")) {
					List<Object> links = (List<Object>) collection.get("links");
					for (Object linkObj : links) {
						Map<String, Object> link = (Map<String, Object>) linkObj;
						if (link.get("rel").equals("items")) {
							collectionsMap.add(collection);
							break;
						}
					}
				}
				if (noOfCollections > 0 && collectionsMap.size() >= noOfCollections) {
					return collectionsMap;
				}
				else if (collectionsMap.size() >= OgcApiFeatures10.COLLECTIONS_LIMIT) {
					return collectionsMap.subList(0, OgcApiFeatures10.COLLECTIONS_LIMIT);
				}
			}
		}
		return collectionsMap;
	}

	/**
	 * Abstract Test 11 (v1.0.1)
	 *
	 * <pre>
	 * Abstract Test 11 (v1.0.1): /conf/core/fc-md-links
	 * Test Purpose: Validate that the required links are included in the Collections Metadata document.
	 * Requirement: /req/core/fc-md-links
	 *
	 * </pre> Test Method
	 * <ul>
	 * <li>Verify that the response document includes:
	 * <ul>
	 * <li>a link to this response document (relation: self),</li>
	 * <li>a link to the response document in every other media type supported by the
	 * server (relation: alternate).</li>
	 * </ul>
	 * </li>
	 * <li>Verify that all links include the rel and type link parameters.</li>
	 * </ul>
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 11 (v1.0.1): /conf/core/fc-md-links",
			groups = "collections", dataProvider = "collectionsUris",
			dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
	public void validateFeatureCollectionsMetadataOperationResponse_Links_v101(TestPoint testPoint) {
		Response response = testPointAndResponses.get(testPoint);
		if (response == null)
			throw new SkipException("Could not find a response for test point " + testPoint);

		JsonPath jsonPath = response.jsonPath();
		List<Map<String, Object>> links = parseAsListOfMaps("links", jsonPath);

		// Requirement 13 A (1): a link to this response document (relation: self)
		Map<String, Object> linkToSelf = findLinkByRel(links, "self");
		assertNotNull(linkToSelf, "Feature Collections Metadata document must include a link for itself");
		// Requirement 13B: All links SHALL include the rel and type link parameters.
		assertTrue(linkIncludesRelAndType(linkToSelf), "Link to itself must include a rel and type parameter");

		// Requirement 13 A (2): a link to the response document in every other media
		// type
		// supported by the server
		// (relation: alternate)
		// Dev: Supported media type are identified by the compliance classes for this
		// server
		List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForOtherResources(linkToSelf);
		List<Map<String, Object>> alternateLinks = findLinksWithSupportedMediaTypeByRel(links, mediaTypesToSupport,
				"alternate");
		List<String> typesWithoutLink = findUnsupportedTypes(alternateLinks, mediaTypesToSupport);
		assertTrue(typesWithoutLink.isEmpty(),
				"Feature Collections Metadata document must include links for alternate encodings. Missing links for types "
						+ typesWithoutLink);

		// Requirement 13 B: All "self"/"alternate" links SHALL include the rel and type
		// link parameters.
		Set<String> rels = new HashSet<>();
		rels.add("self");
		rels.add("alternate");
		List<String> linksWithoutRelOrType = findLinksWithoutRelOrType(alternateLinks, rels);
		assertTrue(linksWithoutRelOrType.isEmpty(),
				"Links for alternate encodings must include a rel and type parameter. Missing for links "
						+ linksWithoutRelOrType);
	}

	/**
	 * Abstract Test 12 (v1.0.1)
	 *
	 * <pre>
	 * Abstract Test 12 (v1.0.1): /conf/core/fc-md-items
	 * Test Purpose: Validate that each collection provided by the server is described in the Collections Metadata.
	 * Requirement: /req/core/fc-md-items
	 *
	 * </pre> Test Method
	 * <ul>
	 * <li>Verify that there is an entry in the collections array of the Collections
	 * Metadata for each feature collection provided by the API.</li>
	 * <li>Verify that each collection entry includes an identifier.</li>
	 * <li>Verify that each collection entry includes links in accordance with
	 * /conf/core/fc-md-items-links.</li>
	 * <li>Verify that if the collection entry includes an extent property, that that
	 * property complies with /conf/core/fc-md-extent</li>
	 * <li>Validate each collection entry for all supported media types using the
	 * resources and tests identified in the table below. <br/>
	 * The collection entries may be encoded in a number of different formats. The
	 * following table identifies the applicable schema document for each format and the
	 * test to be used to validate the against that schema. All supported formats should
	 * be exercised. <br/>
	 * <table>
	 * <caption>Table: Schema and Tests for Collection Entries</caption> <thead>
	 * <tr>
	 * <th>Format</th>
	 * <th>Schema Document</th>
	 * <th>Test ID</th>
	 * </tr>
	 * </thead> <tbody>
	 * <tr>
	 * <td>HTML</td>
	 * <td>collection.yaml</td>
	 * <td>Manual Inspection</td>
	 * </tr>
	 * <tr>
	 * <td>GeoJSON</td>
	 * <td>collection.yaml</td>
	 * <td>/conf/geojson/content</td>
	 * </tr>
	 * <tr>
	 * <td>GMLSF0</td>
	 * <td>core.xsd</td>
	 * <td>/conf/gmlsf0/content</td>
	 * </tr>
	 * <tr>
	 * <td>GMLSF2</td>
	 * <td>core.xsd</td>
	 * <td>/conf/gmlsf2/content</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * </li>
	 * </ul>
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 11 (v1.0.1): /conf/core/fc-md-links",
			groups = "collections", dataProvider = "collectionsUris",
			dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
	public void validateFeatureCollectionsMetadataOperationResponse_Items_v101(TestPoint testPoint) {
		Response response = testPointAndResponses.get(testPoint);
		if (response == null)
			throw new SkipException("Could not find a response for test point " + testPoint);

		JsonPath jsonPath = response.jsonPath();

		List<Map<String, Object>> collections = parseAsListOfMaps("collections", jsonPath);

		for (Map<String, Object> collection : collections) {

			// Verify that each collection entry includes an identifier.
			Object id = collection.get("id");
			assertNotNull(id);
			assertTrue(id instanceof String, "Id is not a String, is: " + id.getClass());
			assertTrue(!((String) id).isEmpty(), "Id is empty.");

			// Verify that each collection entry includes links in accordance with
			// /conf/core/fc-md-links
			// will be tested by
			// validateFeatureCollectionsMetadataOperationResponse_Items_Links_v101 method

			// Verify that if the collection entry includes an extent property, that that
			// property complies with /conf/core/fc-md-extent
			try {
				JsonUtils.parseSpatialExtent(collection);
			}
			catch (Exception e) {
				fail("Could not parse spatial extent for collection with id:" + id);
			}

			try {
				JsonUtils.parseTemporalExtent(collection);
			}
			catch (Exception e) {
				TestSuiteLogger.log(Level.INFO, e.getMessage());
				fail("Could not parse temporal extent for collection with id: " + id);
			}

			// Validate each collection entry for all supported media types using the
			// resources and tests identified in the table below.
			List<Map<String, Object>> links = parseAsListOfMaps("links", collection);
			List<Map<String, Object>> linksToItems = findLinksByRel(links, "items");
			assertNotNull(linksToItems, "Feature Collections Metadata document must include a link for items");
			for (Map<String, Object> linkToItems : linksToItems) {
				String type = (String) linkToItems.get("type");
				String href = (String) linkToItems.get("href");
				Client client = ClientUtils.buildClient();
				WebTarget target = client.target(href);
				Builder builder = target.request(type);
				jakarta.ws.rs.core.Response rsp = builder.buildGet().invoke();
				switch (type) {
					case "application/geo+json": {
						try {
							JsonPath collectionAsPath = JsonPath.from((InputStream) rsp.getEntity());
							String typeElementAsString = collectionAsPath.get("type");
							assertNotNull(typeElementAsString);
							assertTrue(typeElementAsString.equals("FeatureCollection"),
									"Json not of type FeatureCollection, was: " + typeElementAsString);
							continue;
						}
						catch (Exception e) {
							TestSuiteLogger.log(Level.WARNING,
									String.format("Failed to test content type '%s'.", type));
							fail(e.getMessage());
						}
					}
					case "application/gml+xml;version=3.2":
					case "application/gml+xml;version=3.2;profile=\"http://www.opengis.net/def/profile/ogc/2.0/gml-sf0\"":
					case "application/gml+xml;version=3.2;profile=\"http://www.opengis.net/def/profile/ogc/2.0/gml-sf2\"": {
						try {
							Document collectionAsDocument = DocumentBuilderFactory.newDefaultInstance()
								.newDocumentBuilder()
								.parse((InputStream) rsp.getEntity());
							assertNotNull(collectionAsDocument.getElementsByTagName("FeatureCollection"));
							continue;
						}
						catch (Exception e) {
							TestSuiteLogger.log(Level.WARNING,
									String.format("Failed to test content type '%s'.", type));
							fail(e.getMessage());
						}
					}
					default:
						TestSuiteLogger.log(Level.INFO, String.format("Skipping content type '%s'.", type));
				}
			}
		}

	}

	/**
	 * Abstract Test 13 (v1.0.1)
	 *
	 * <pre>
	 * Abstract Test 13 (v1.0.1): /conf/core/fc-md-items-links
	 * Test Purpose: Validate that each Feature Collection metadata entry in the Collections Metadata document includes all required links.
	 * Requirement: /req/core/fc-md-items-links
	 *
	 * </pre> Test Method
	 * <ul>
	 * <li>Verify that each Collection item in the Collections Metadata document includes
	 * a link property for each supported encoding.</li>
	 * <li>Verify that the links properties of the collection includes an item for each
	 * supported encoding with a link to the features resource (relation: items).</li>
	 * <li>Verify that all links include the rel and type link parameters.</li>
	 * </ul>
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 13 (v1.0.1): /conf/core/fc-md-items-links",
			groups = "collections", dataProvider = "collectionsUris",
			dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
	public void validateFeatureCollectionsMetadataOperationResponse_Items_Links_v101(TestPoint testPoint) {
		Response response = testPointAndResponses.get(testPoint);
		if (response == null)
			throw new SkipException("Could not find a response for test point " + testPoint);

		JsonPath jsonPath = response.jsonPath();

		List<Map<String, Object>> collections = parseAsListOfMaps("collections", jsonPath);

		for (Map<String, Object> collection : collections) {

			// Verify that each collection entry includes links in accordance with
			// /conf/core/fc-md-links
			List<Map<String, Object>> links = parseAsListOfMaps("links", collection);
			Map<String, Object> linkToItems = findLinkByRel(links, "items");
			assertNotNull(linkToItems, "Feature Collections Metadata document must include a link for items");

			// Verify that the links properties of the collection includes an item for
			// each
			// supported encoding with a link to the features resource (relation: items)
			List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForOtherResources(linkToItems);

			List<String> supportedEncodingLinksWithoutRel = findSupportedEncodingLinksWithoutRel(links,
					mediaTypesToSupport);

			assertTrue(supportedEncodingLinksWithoutRel.isEmpty(),
					"Links to supported encoding must contain a href. Missing hrefs for types "
							+ supportedEncodingLinksWithoutRel);

			// Verify that all links include the rel and type link parameters.
			for (Map<String, Object> link : links) {
				assertTrue(linkIncludesRelAndType(link),
						"Links must contain rel and type parameters. Missing for: " + link);
			}
		}
	}

	/**
	 * Abstract Test 14 (v1.0.1)
	 *
	 * <pre>
	 * Abstract Test 14 (v1.0.1): /conf/core/fc-md-extent
	 * Test Purpose: Validate the extent property, if it is present.
	 * Requirement: /req/core/fc-md-extent
	 *
	 * Test Method
	 * Verify that the extent, if present, provides bounding boxes that include all spatial geometries in this collection.
	 *
	 * Verify that the extent, if present, provides time intervals that include all temporal geometries in this collection. A temporal boundary of null at start or end indicates a half-bounded interval.
	 * </pre>
	 * @param testPoint the test point to test, never <code>null</code>
	 */
	@Test(description = "A.2.5. Feature Collections {root}/collections, Abstract Test 11 (v1.0.1): /conf/core/fc-md-links",
			groups = "collections", dataProvider = "collectionsUris",
			dependsOnMethods = "validateFeatureCollectionsMetadataOperation", alwaysRun = true)
	public void validateFeatureCollectionsMetadataOperationResponse_Extent_v101(TestPoint testPoint) {
		Response response = testPointAndResponses.get(testPoint);
		if (response == null)
			throw new SkipException("Could not find a response for test point " + testPoint);

		JsonPath jsonPath = response.jsonPath();
		List<Map<String, Object>> links = parseAsListOfMaps("links", jsonPath);

		// Requirement 13 A (1): a link to this response document (relation: self)
		Map<String, Object> linkToSelf = findLinkByRel(links, "self");
		assertNotNull(linkToSelf, "Feature Collections Metadata document must include a link for itself");
		// Requirement 13B: All links SHALL include the rel and type link parameters.
		assertTrue(linkIncludesRelAndType(linkToSelf), "Link to itself must include a rel and type parameter");

		// Requirement 13 A (2): a link to the response document in every other media
		// type
		// supported by the server
		// (relation: alternate)
		// Dev: Supported media type are identified by the compliance classes for this
		// server
		List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForOtherResources(linkToSelf);
		List<Map<String, Object>> alternateLinks = findLinksWithSupportedMediaTypeByRel(links, mediaTypesToSupport,
				"alternate");
		List<String> typesWithoutLink = findUnsupportedTypes(alternateLinks, mediaTypesToSupport);
		assertTrue(typesWithoutLink.isEmpty(),
				"Feature Collections Metadata document must include links for alternate encodings. Missing links for types "
						+ typesWithoutLink);

		// Requirement 13 B: All "self"/"alternate" links SHALL include the rel and type
		// link parameters.
		Set<String> rels = new HashSet<>();
		rels.add("self");
		rels.add("alternate");
		List<String> linksWithoutRelOrType = findLinksWithoutRelOrType(alternateLinks, rels);
		assertTrue(linksWithoutRelOrType.isEmpty(),
				"Links for alternate encodings must include a rel and type parameter. Missing for links "
						+ linksWithoutRelOrType);
	}

}
