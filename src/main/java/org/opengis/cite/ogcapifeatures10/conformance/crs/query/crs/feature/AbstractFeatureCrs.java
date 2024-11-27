package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.CommonFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.CoordinateSystem;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import io.restassured.path.json.JsonPath;

/**
 * <p>AbstractFeatureCrs class.</p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class AbstractFeatureCrs extends CommonFixture {

	protected Map<String, JsonPath> collectionsResponses;

	protected Map<String, List<CoordinateSystem>> collectionIdToCrs;

	protected Map<String, CoordinateSystem> collectionIdToDefaultCrs;

	protected Map<String, String> collectionIdToFeatureId;

	/**
	 * <p>retrieveRequiredInformationFromTestContext.</p>
	 *
	 * @param testContext a {@link org.testng.ITestContext} object
	 */
	@BeforeClass
	public void retrieveRequiredInformationFromTestContext(ITestContext testContext) {
		this.collectionsResponses = (Map<String, JsonPath>) testContext.getSuite()
			.getAttribute(SuiteAttribute.COLLECTION_BY_ID.getName());
		this.collectionIdToCrs = (Map<String, List<CoordinateSystem>>) testContext.getSuite()
			.getAttribute(SuiteAttribute.COLLECTION_CRS_BY_ID.getName());
		this.collectionIdToDefaultCrs = (Map<String, CoordinateSystem>) testContext.getSuite()
			.getAttribute(SuiteAttribute.COLLECTION_DEFAULT_CRS_BY_ID.getName());
		this.collectionIdToFeatureId = (Map<String, String>) testContext.getSuite()
			.getAttribute(SuiteAttribute.FEATUREIDS.getName());
	}

	/**
	 * <p>collectionFeatureId.</p>
	 *
	 * @param testContext a {@link org.testng.ITestContext} object
	 * @return a {@link java.util.Iterator} object
	 */
	@DataProvider(name = "collectionFeatureId")
	public Iterator<Object[]> collectionFeatureId(ITestContext testContext) {
		List<Object[]> collectionsData = new ArrayList<>();
		try {
			for (Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet()) {
				String collectionId = collection.getKey();
				if (collectionIdToFeatureId != null && collectionIdToFeatureId.containsKey(collectionId)) {
					String featureId = collectionIdToFeatureId.get(collectionId);
					JsonPath json = collection.getValue();
					collectionsData.add(new Object[] { collectionId, json, featureId });
				}
			}
		}
		catch (Exception e) {
			collectionsData.add(new Object[] { null, null, null });
		}
		return collectionsData.iterator();
	}

}
