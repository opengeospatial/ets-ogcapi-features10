package org.opengis.cite.ogcapifeatures10.conformance.crs.query.crs.features;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.conformance.CommonFixture;
import org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class AbstractFeaturesCrs extends CommonFixture {

    private Map<String, JsonPath> collectionsResponses;

    private Map<String, List<String>> collectionIdToCrs;

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.collectionsResponses = (Map<String, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_BY_ID.getName() );
        this.collectionIdToCrs = (Map<String, List<String>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_CRS_BY_ID.getName() );
    }

    @DataProvider(name = "collectionIdAndJson")
    public Iterator<Object[]> collectionIdAndJson( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            JsonPath json = collection.getValue();
            collectionsData.add( new Object[] { collectionId, json } );
        }
        return collectionsData.iterator();
    }

    @DataProvider(name = "collectionIdAndJsonAndCrs")
    public Iterator<Object[]> collectionIdAndJsonAndCrs( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            JsonPath json = collection.getValue();
            for ( String crs : collectionIdToCrs.get( collectionId ) ) {
                collectionsData.add( new Object[] { collectionId, json, crs } );
            }
        }
        return collectionsData.iterator();
    }

}
