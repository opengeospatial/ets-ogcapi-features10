package org.opengis.cite.wfs30.collections;

import static io.restassured.http.Method.GET;
import static org.opengis.cite.wfs30.WFS3.GEOJSON_MIME_TYPE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.wfs30.CommonFixture;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetFeatureOperation extends CommonFixture {

    private List<Map<String, Object>> collections;

    @DataProvider(name = "collectionFeatureId")
    public Iterator<Object[]> collectionFeatureId( ITestContext testContext ) {
        Map<String, String> collectionNameToFeatureId = (Map<String, String>) testContext.getSuite().getAttribute( SuiteAttribute.FEATUREIDS.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            String collectionName = (String) collection.get( "name" );
            String featureId = collectionNameToFeatureId.get( collectionName );
            collectionsData.add( new Object[] { collection, featureId } );
        }
        return collectionsData.iterator();
    }

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.collections = (List<Map<String, Object>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS.getName() );
    }

    /**
     * A.4.4.14. Get Feature Operation
     *
     * a) Test Purpose: Validate that the Get Feature Operation behaves as required.
     *
     * b) Pre-conditions:
     *
     * A feature collection name is provided by test A.4.4.6
     *
     * A feature identifier is provided by test A.4.4.10
     *
     * Path = /collections/{name}/items/(id} where {id} = the feature identifier
     *
     * c) Test Method:
     *
     * DO FOR each /collections{name}/items/{id} test point
     *
     * Issue an HTTP GET request using the test point URI
     *
     * Go to test A.4.4.15
     *
     * d) References: Requirement 30
     *
     * @param collection
     *            * the collection under test, never <code>null</code>
     * @param featureId
     *            the featureId to request, may be <code>null</code> (test will be skipped)
     */
    @Test(description = "Implements A.4.4.14. Get Feature Operation (Requirement 30)", dataProvider = "collectionFeatureId", dependsOnGroups = "getFeaturesBase")
    public void validateGetFeatureOperation( Map<String, Object> collection, String featureId ) {
        String collectionName = (String) collection.get( "name" );
        if ( featureId == null )
            throw new SkipException( "No featureId available for collection '" + collectionName + "'" );

        String getFeatureUrl = findGetFeatureUrlForGeoJson( collection );
        if ( getFeatureUrl == null )
            throw new SkipException( "Could not find url for collection with name " + collectionName
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        String getFeatureUrlWithFeatureId = getFeatureUrl + "/" + featureId;

        Response response = init().baseUri( getFeatureUrlWithFeatureId ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );
    }

    private String findGetFeatureUrlForGeoJson( Map<String, Object> collection ) {
        List<Object> links = (List<Object>) collection.get( "links" );
        for ( Object linkObject : links ) {
            Map<String, Object> link = (Map<String, Object>) linkObject;
            Object rel = link.get( "rel" );
            Object type = link.get( "type" );
            if ( "item".equals( rel ) && GEOJSON_MIME_TYPE.equals( type ) )
                return (String) link.get( "href" );
        }
        return null;
    }

}