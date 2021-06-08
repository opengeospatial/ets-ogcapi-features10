package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.CommonFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.opengis.cite.ogcapifeatures10.util.JsonUtils;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class AbstractFeatureCrs extends CommonFixture {

    @DataProvider(name = "collectionFeatureIdCrs")
    public Iterator<Object[]> collectionFeatureIdCrs( ITestContext testContext ) {
        Map<String, JsonPath> collectionsResponses = (Map<String, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_TO_ID.getName() );
        Map<String, String> collectionNameToFeatureId = (Map<String, String>) testContext.getSuite().getAttribute( SuiteAttribute.FEATUREIDS.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            String featureId = null;
            if ( collectionNameToFeatureId != null )
                featureId = collectionNameToFeatureId.get( collectionId );
            JsonPath json = collection.getValue();
            for ( String crs : JsonUtils.parseAsList( "crs", json ) ) {
                collectionsData.add( new Object[] { collectionId, json, featureId, crs } );
            }
        }
        return collectionsData.iterator();
    }

    @DataProvider(name = "collectionFeatureId")
    public Iterator<Object[]> collectionFeatureId( ITestContext testContext ) {
        Map<String, JsonPath> collectionsResponses = (Map<String, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_TO_ID.getName() );
        Map<String, String> collectionNameToFeatureId = (Map<String, String>) testContext.getSuite().getAttribute( SuiteAttribute.FEATUREIDS.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            if ( collectionNameToFeatureId != null && collectionNameToFeatureId.containsKey( collectionId ) ) {
                String featureId = collectionNameToFeatureId.get( collectionId );
                JsonPath json = collection.getValue();
                collectionsData.add( new Object[] { collectionId, json, featureId } );
            }
        }
        return collectionsData.iterator();
    }
}
