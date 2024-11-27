package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.features;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.OgcApiFeatures10;
import org.opengis.cite.ogcapifeatures10.conformance.CommonFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class AbstractFeaturesCrs extends CommonFixture {

	private Map<String, JsonPath> collectionsResponses;

	private Map<String, List<CoordinateSystem>> collectionIdToCrs;

	private Map<String, CoordinateSystem> collectionIdToDefaultCrs;

	@BeforeClass
	public void retrieveRequiredInformationFromTestContext(ITestContext testContext) {
		this.collectionsResponses = (Map<String, JsonPath>) testContext.getSuite()
			.getAttribute(SuiteAttribute.COLLECTION_BY_ID.getName());
		this.collectionIdToCrs = (Map<String, List<CoordinateSystem>>) testContext.getSuite()
			.getAttribute(SuiteAttribute.COLLECTION_CRS_BY_ID.getName());
		this.collectionIdToDefaultCrs = (Map<String, CoordinateSystem>) testContext.getSuite()
			.getAttribute(SuiteAttribute.COLLECTION_DEFAULT_CRS_BY_ID.getName());
	}

	@DataProvider(name = "collectionIdAndJson")
	public Iterator<Object[]> collectionIdAndJson(ITestContext testContext) {
		List<Object[]> collectionsData = new ArrayList<>();
		try {
			for (Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet()) {
				String collectionId = collection.getKey();
				JsonPath json = collection.getValue();
				collectionsData.add(new Object[] { collectionId, json });
			}
		}
		catch (Exception e) {
			collectionsData.add(new Object[] { null, null });
		}
		return collectionsData.iterator();
	}

	@DataProvider(name = "collectionIdAndJsonAndCrs")
	public Iterator<Object[]> collectionIdAndJsonAndCrs(ITestContext testContext) {
		List<Object[]> collectionsData = new ArrayList<>();
		try {
			for (Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet()) {
				String collectionId = collection.getKey();
				JsonPath json = collection.getValue();
				int count = 0;
				for (CoordinateSystem crs : collectionIdToCrs.get(collectionId)) {
					if (count >= OgcApiFeatures10.CRS_LIMIT) {
						break;
					}
					collectionsData.add(new Object[] { collectionId, json, crs });
					count++;
				}
			}
		}
		catch (Exception e) {
			collectionsData.add(new Object[] { null, null, null });
		}
		return collectionsData.iterator();
	}

	@DataProvider(name = "collectionIdAndJsonAndCrsAndDefaultCrs")
	public Iterator<Object[]> collectionIdAndJsonAndCrsAndDefaultCrs(ITestContext testContext) {
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

}
