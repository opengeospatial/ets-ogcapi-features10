package org.opengis.cite.ogcapifeatures10.conformance.crs.query.bboxcrs;

import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseSpatialExtent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.opengis.cite.ogcapifeatures10.util.BBox;
import org.opengis.cite.ogcapifeatures10.util.GeometryTransformer;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * <pre>
 * Abstract Test 8: /conf/crs/bbox-crs-parameter
 * Test Purpose: Verify that the parameter bbox-crs has been implemented correctly
 * Requirement: /req/crs/fc-bbox-crs-definition, /req/crs/bbox-crs-action
 *
 * Test Method
 * For every CRS identifier advertized by the Web API that is known to the test engine and for which the test engine can convert geometries between the CRS and the default CRS of the Web API ("known CRS") execute the following test. Skip the test for unknown CRSs.
 *  1. For each spatial feature collection collectionId and every GML or GeoJSON feature representation supported by the Web API, send a request with the parameters bbox and bbox-crs to /collections/{collectionId}/items for every known CRS. Use a bbox value in the spatial extent of the collection, converted to the known CRS. Send the same request, but with no bbox-crs parameter and a bbox value in the default CRS. Do not include a crs parameter in the requests. Verify that the responses include the same features.
 * </pre>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BBoxCrsParameter extends AbstractBBoxCrs {

	private Map<String, Response> collectionIdToResponseWithDefaultCRs = new HashMap<>();

	private Map<String, BBox> collectionIdToSpatialExtent = new HashMap<String, BBox>();

	/**
	 * <p>
	 * collectionDefaultCrs.
	 * </p>
	 * @param testContext a {@link org.testng.ITestContext} object
	 * @return a {@link java.util.Iterator} object
	 */
	@DataProvider(name = "collectionDefaultCrs")
	public Iterator<Object[]> collectionDefaultCrs(ITestContext testContext) {
		List<Object[]> collectionsData = new ArrayList<>();
		try {
			for (Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet()) {
				String collectionId = collection.getKey();
				JsonPath json = collection.getValue();
				CoordinateSystem defaultCrs = collectionIdToDefaultCrs.get(collectionId);
				if (defaultCrs != null) {
					collectionsData.add(new Object[] { collectionId, json, defaultCrs });
				}
			}
		}
		catch (Exception e) {
			collectionsData.add(new Object[] { null, null, null });
		}
		return collectionsData.iterator();
	}

	/**
	 * <p>
	 * collectionCrs.
	 * </p>
	 * @param testContext a {@link org.testng.ITestContext} object
	 * @return a {@link java.util.Iterator} object
	 */
	@DataProvider(name = "collectionCrsAndDefaultCrs")
	public Iterator<Object[]> collectionCrs(ITestContext testContext) {
		List<Object[]> collectionsData = new ArrayList<>();
		try {
			for (Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet()) {
				String collectionId = collection.getKey();
				JsonPath json = collection.getValue();
				CoordinateSystem defaultCrs = collectionIdToDefaultCrs.get(collectionId);
				if (defaultCrs != null) {
					for (CoordinateSystem crs : collectionIdToCrs.get(collectionId)) {
						collectionsData.add(new Object[] { collectionId, json, crs, defaultCrs });
					}
				}
			}
		}
		catch (Exception e) {
			collectionsData.add(new Object[] { null, null, null, null });
		}
		return collectionsData.iterator();
	}

	/**
	 * <p>
	 * verifyBboxCrsParameterWithDefaultCrs.
	 * </p>
	 * @param collectionId the id of the collection, never <code>null</code>
	 * @param collection the /collection object, never <code>null</code>
	 * @param defaultCrs the defaultCrs of the collection, never <code>null</code>
	 */
	@Test(description = "Implements A.2.2 Query, Parameter bbox-crs, Abstract Test 8 (Requirement /req/crs/fc-bbox-crs-definition, /req/crs/bbox-crs-action)",
			dataProvider = "collectionDefaultCrs", dependsOnGroups = "crs-conformance", priority = 1)
	public void verifyBboxCrsParameterWithDefaultCrs(String collectionId, JsonPath collection,
			CoordinateSystem defaultCrs) {
		if ((collectionId == null) & (collection == null) & (defaultCrs == null)) {
			throw new AssertionError("No crs information for collection available.");
		}
		String featuredUrl = JsonUtils.findFeaturesUrlForGeoJson(rootUri, collection);
		if (featuredUrl == null)
			throw new SkipException(
					String.format("Could not find url for collection with id %s supporting GeoJson (type 5s)",
							collectionId, GEOJSON_MIME_TYPE));
		BBox bbox = parseSpatialExtent(collection.get());
		if (bbox == null)
			throw new SkipException(String.format("Collection with id %s has no spatial extent", collectionId));
		GeometryTransformer geometryTransformer = new GeometryTransformer(bbox.getCrs(), defaultCrs);
		BBox transformedBboxInDefaultCrs = geometryTransformer.transform(bbox);
		String bboxParameterValueDefaultCrs = transformedBboxInDefaultCrs.asQueryParameter();
		Response response = init().baseUri(featuredUrl)
			.accept(GEOJSON_MIME_TYPE)
			.param(BBOX_PARAM, bboxParameterValueDefaultCrs)
			.when()
			.request(Method.GET);
		response.then().statusCode(200);

		collectionIdToResponseWithDefaultCRs.put(collectionId, response);
		collectionIdToSpatialExtent.put(collectionId, bbox);

	}

	/**
	 * <p>
	 * verifyBboxCrsParameter.
	 * </p>
	 * @param collectionId the id of the collection, never <code>null</code>
	 * @param collection the /collection object, never <code>null</code>
	 * @param crs the crs to test, never <code>null</code>
	 * @param defaultCrs the defaultCrs of the collection, never <code>null</code>
	 */
	@Test(description = "Implements A.2.2 Query, Parameter bbox-crs, Abstract Test 8 (Requirement /req/crs/fc-bbox-crs-definition, /req/crs/bbox-crs-action)",
			dataProvider = "collectionCrsAndDefaultCrs", dependsOnGroups = "crs-conformance",
			dependsOnMethods = "verifyBboxCrsParameterWithDefaultCrs", priority = 1)
	public void verifyBboxCrsParameter(String collectionId, JsonPath collection, CoordinateSystem crs,
			CoordinateSystem defaultCrs) {
		if ((collectionId == null) & (collection == null) & (crs == null) & (defaultCrs == null)) {
			throw new AssertionError("No crs information for collection available.");
		}
		if (!collectionIdToResponseWithDefaultCRs.containsKey(collectionId))
			throw new SkipException(String
				.format("Collection with id %s could not be requested with bbox in default crs", collectionId));
		String featuredUrl = JsonUtils.findFeaturesUrlForGeoJson(rootUri, collection);
		if (featuredUrl == null)
			throw new SkipException(
					String.format("Could not find url for collection with id %s supporting GeoJson (type 5s)",
							collectionId, GEOJSON_MIME_TYPE));
		BBox bbox = collectionIdToSpatialExtent.get(collectionId);
		GeometryTransformer geometryTransformer = new GeometryTransformer(bbox.getCrs(), crs);

		// https://github.com/opengeospatial/ets-ogcapi-features10/issues/199
		// Transforming the maximum extent in WGS 84 to another CRS can cause problems.
		// In that case, we make the extent a bit smaller.
		BBox maxExtent = new BBox(-180, -90, 180, 90);

		if (bbox.equals(maxExtent)) {
			if (!(crs.getCode().equals("http://www.opengis.net/def/crs/EPSG/0/4326")
					|| crs.getCode().equals("http://www.opengis.net/def/crs/OGC/1.3/CRS84"))) {
				bbox = new BBox(-175, -85, 175, 85);
			}
		}

		BBox transformedBbox = geometryTransformer.transform(bbox);

		Response response = init().baseUri(featuredUrl)
			.param(BBOX_CRS_PARAM, transformedBbox.getCrs().getCode())
			.param(BBOX_PARAM, transformedBbox.asQueryParameter())
			.accept(GEOJSON_MIME_TYPE)
			.when()
			.request(Method.GET);
		response.then().statusCode(200);

		Response responseWithBBoxInDefaultCrs = collectionIdToResponseWithDefaultCRs.get(collectionId);
		assertSameFeatures(response.jsonPath(), responseWithBBoxInDefaultCrs.jsonPath());
	}

}
