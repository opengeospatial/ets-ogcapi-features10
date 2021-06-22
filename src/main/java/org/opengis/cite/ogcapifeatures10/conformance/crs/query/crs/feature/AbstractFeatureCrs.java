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
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class AbstractFeatureCrs extends CommonFixture {

    private Map<String, JsonPath> collectionsResponses;

    private Map<String, List<CoordinateSystem>> collectionIdToCrs;

    private Map<String, String> collectionIdToFeatureId;

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.collectionsResponses = (Map<String, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_BY_ID.getName() );
        this.collectionIdToCrs = (Map<String, List<CoordinateSystem>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_CRS_BY_ID.getName() );
        this.collectionIdToFeatureId = (Map<String, String>) testContext.getSuite().getAttribute( SuiteAttribute.FEATUREIDS.getName() );
    }

    @DataProvider(name = "collectionFeatureIdCrs")
    public Iterator<Object[]> collectionFeatureIdCrs( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            String featureId = null;
            if ( collectionIdToFeatureId != null )
                featureId = collectionIdToFeatureId.get( collectionId );
            JsonPath json = collection.getValue();
            for ( CoordinateSystem crs : collectionIdToCrs.get( collectionId ) ) {
                collectionsData.add( new Object[] { collectionId, json, featureId, crs } );
            }
        }
        return collectionsData.iterator();
    }

    @DataProvider(name = "collectionFeatureId")
    public Iterator<Object[]> collectionFeatureId( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            if ( collectionIdToFeatureId != null && collectionIdToFeatureId.containsKey( collectionId ) ) {
                String featureId = collectionIdToFeatureId.get( collectionId );
                JsonPath json = collection.getValue();
                collectionsData.add( new Object[] { collectionId, json, featureId } );
            }
        }
        return collectionsData.iterator();
    }
}
