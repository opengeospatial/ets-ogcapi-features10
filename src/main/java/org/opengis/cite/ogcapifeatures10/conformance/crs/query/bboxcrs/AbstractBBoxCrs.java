package org.opengis.cite.ogcapifeatures10.conformance.crs.query.bboxcrs;

import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseAsString;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class AbstractBBoxCrs extends CommonFixture {

    public static final String BBOX_PARAM = "bbox";

    public static final String BBOX_CRS_PARAM = "bbox-crs";

    private Map<String, JsonPath> collectionsResponses;

    private Map<String, List<CoordinateSystem>> collectionIdToCrs;

    private Map<String, CoordinateSystem> collectionIdToDefaultCrs;

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.collectionsResponses = (Map<String, JsonPath>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_BY_ID.getName() );
        this.collectionIdToCrs = (Map<String, List<CoordinateSystem>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_CRS_BY_ID.getName() );
        this.collectionIdToDefaultCrs = (Map<String, CoordinateSystem>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTION_DEFAULT_CRS_BY_ID.getName() );
    }

    @DataProvider(name = "collectionCrs")
    public Iterator<Object[]> collectionCrs( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            JsonPath json = collection.getValue();
            for ( CoordinateSystem crs : collectionIdToCrs.get( collectionId ) ) {
                collectionsData.add( new Object[] { collectionId, json, crs } );
            }
        }
        return collectionsData.iterator();
    }

    @DataProvider(name = "collectionDefaultCrs")
    public Iterator<Object[]> collectionDefaultCrs( ITestContext testContext ) {
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map.Entry<String, JsonPath> collection : collectionsResponses.entrySet() ) {
            String collectionId = collection.getKey();
            JsonPath json = collection.getValue();
            CoordinateSystem defaultCrs = collectionIdToDefaultCrs.get( collectionId );
            collectionsData.add( new Object[] { collectionId, json, defaultCrs } );
        }
        return collectionsData.iterator();
    }

    void assertSameFeatures( JsonPath responseWithBBox, JsonPath responseWithoutBBox ) {
        List<String> responseWithBBoxIds = parseFeatureIds( responseWithBBox );
        List<String> responseWithoutBBoxIds = parseFeatureIds( responseWithoutBBox );
        assertEquals( responseWithBBoxIds, responseWithoutBBoxIds );
    }

    private List<String> parseFeatureIds( JsonPath responseWithBBox ) {
        List<Map<String, Object>> features = responseWithBBox.getList( "features" );
        return features.stream().map( feature -> parseAsString( feature.get( "id" ) ) ).collect( Collectors.toList() );
    }

}
