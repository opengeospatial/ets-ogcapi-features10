package org.opengis.cite.ogcapifeatures10.simpletransactions;

import static io.restassured.http.Method.DELETE;
import static io.restassured.http.Method.GET;
import static io.restassured.http.Method.POST;
import static org.opengis.cite.ogcapifeatures10.OgcApiFeatures10.GEOJSON_MIME_TYPE;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapifeatures10.CommonDataFixture;
import org.opengis.cite.ogcapifeatures10.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * Insert
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 */
public class Insert extends CommonDataFixture {

    private List<Map<String, Object>> collections;

    private String getFeatureUrl;

    private List<String> featureIds = new ArrayList<>();

    @DataProvider(name = "collectionFeatureId")
    public Iterator<Object[]> collectionFeatureId( ITestContext testContext ) {
        Map<String, String> collectionNameToFeatureId = (Map<String, String>) testContext.getSuite().getAttribute( SuiteAttribute.FEATUREIDS.getName() );
        List<Object[]> collectionsData = new ArrayList<>();
        for ( Map<String, Object> collection : collections ) {
            String collectionId = (String) collection.get( "id" );
            String featureId = null;
            if ( collectionNameToFeatureId != null )
                featureId = collectionNameToFeatureId.get( collectionId );
            collectionsData.add( new Object[] { collection, featureId } );
        }
        return collectionsData.iterator();
    }

    @BeforeClass
    public void retrieveRequiredInformationFromTestContext( ITestContext testContext ) {
        this.collections = (List<Map<String, Object>>) testContext.getSuite().getAttribute( SuiteAttribute.COLLECTIONS.getName() );
    }

    @AfterClass
    public void deleteInsertedFeatures() {
        for ( String featureId : featureIds ) {
            Response deleteResponse = init().baseUri( getFeatureUrl + "/" + featureId ).when().request( DELETE );
            System.out.println( "Delete response:" );
            System.out.println( deleteResponse.body().prettyPrint() );
        }
    }

    /**
     * 
     * @param collection
     *            the collection under test, never <code>null</code>
     * @param featureId
     *            the featureId to request, may be <code>null</code> (test will be skipped)
     */
    @Test(description = "Insert test", dataProvider = "collectionFeatureId", dependsOnGroups = "preconditions")
    public void insert( Map<String, Object> collection, String featureId ) {
        String collectionId = (String) collection.get( "id" );
        if ( featureId == null )
            throw new SkipException( "No featureId available for collection '" + collectionId + "'" );

        getFeatureUrl = findGetFeatureUrlForGeoJson( collection );
        if ( getFeatureUrl == null )
            throw new SkipException( "Could not find url for collection with name " + collectionId
                                     + " supporting GeoJson (type " + GEOJSON_MIME_TYPE + ")" );
        String getFeatureUrlWithFeatureId;
        if ( getFeatureUrl.indexOf( "?" ) == -1 ) {
            getFeatureUrlWithFeatureId = getFeatureUrl + "/" + featureId;
        } else {
            getFeatureUrlWithFeatureId = getFeatureUrl.substring( 0, getFeatureUrl.indexOf( "?" ) ) + "/" + featureId;
        }

        Response response = init().baseUri( getFeatureUrlWithFeatureId ).accept( GEOJSON_MIME_TYPE ).when().request( GET );
        response.then().statusCode( 200 );

        if ( response == null )
            throw new SkipException( "Could not find a response for collection with id " + collectionId );

        String body = response.body().print();
        String randomFeatureId = "testid" + Math.random();
        featureIds.add( randomFeatureId );
        String newFeature = body.replace( featureId, randomFeatureId );

        Response insertResponse = init().baseUri( getFeatureUrl ).contentType( GEOJSON_MIME_TYPE ).body( newFeature ).when().request( POST );
        System.out.println( "Insert response:" );
        System.out.println( insertResponse.body().prettyPrint() );
    }

    private String findGetFeatureUrlForGeoJson( Map<String, Object> collection ) {
        List<Object> links = (List<Object>) collection.get( "links" );
        for ( Object linkObject : links ) {
            Map<String, Object> link = (Map<String, Object>) linkObject;
            Object rel = link.get( "rel" );
            Object type = link.get( "type" );
            if ( "items".equals( rel ) && GEOJSON_MIME_TYPE.equals( type ) )
                return (String) link.get( "href" );
        }
        return null;
    }

}