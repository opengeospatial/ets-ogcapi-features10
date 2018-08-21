package org.opengis.cite.wfs30.collections;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.opengis.cite.wfs30.conformance.RequirementClass;
import org.testng.ISuite;
import org.testng.ITestContext;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetFeatureOperationIT {

    public static final String COLLECTION_NAME = "flurstueck";

    private static ITestContext testContext;

    private static ISuite suite;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        OpenApi3Parser parser = new OpenApi3Parser();
        URL openAppiDocument = GetFeatureOperationIT.class.getResource( "../openapi3/openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );

        InputStream json = GetFeatureOperationIT.class.getResourceAsStream( "../collections/collections.json" );
        JsonPath collectionsResponse = new JsonPath( json );
        List<Map<String, Object>> collections = collectionsResponse.getList( "collections" );

        Map<String, String> featureIds = new HashMap<>();
        featureIds.put( COLLECTION_NAME, "DENW19AL0000geMFFL" );

        List<RequirementClass> requirementClasses = new ArrayList();
        requirementClasses.add( RequirementClass.CORE );

        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );

        URI landingPageUri = new URI( "https://www.ldproxy.nrw.de/kataster" );
        when( suite.getAttribute( SuiteAttribute.IUT.getName() ) ).thenReturn( landingPageUri );
        when( suite.getAttribute( SuiteAttribute.API_MODEL.getName() ) ).thenReturn( apiModel );
        when( suite.getAttribute( SuiteAttribute.COLLECTIONS.getName() ) ).thenReturn( collections );
        when( suite.getAttribute( SuiteAttribute.FEATUREIDS.getName() ) ).thenReturn( featureIds );
        when( suite.getAttribute( SuiteAttribute.REQUIREMENTCLASSES.getName() ) ).thenReturn( requirementClasses );
    }

    @Test
    public void testGetFeatureOperations() {
        GetFeatureOperation getFeatureOperation = new GetFeatureOperation();
        getFeatureOperation.initCommonFixture( testContext );
        getFeatureOperation.retrieveRequiredInformationFromTestContext( testContext );
        getFeatureOperation.requirementClasses( testContext );

        Iterator<Object[]> collections = getFeatureOperation.collectionFeatureId( testContext );
        Object[] collectionAndFeatureId = findCollectionByName( COLLECTION_NAME, collections );
        assertThat( collectionAndFeatureId, notNullValue() );

        Map<String, Object> collection = (Map<String, Object>) collectionAndFeatureId[0];
        assertThat( collection, notNullValue() );

        String featureId = (String) collectionAndFeatureId[1];
        assertThat( featureId, notNullValue() );

        getFeatureOperation.getFeatureOperation( collection, featureId );
        getFeatureOperation.validateTheGetFeatureOperationResponse( collection, featureId );
    }

    private Object[] findCollectionByName( String collectionName, Iterator<Object[]> collections ) {
        for ( Iterator<Object[]> it = collections; it.hasNext(); ) {
            Object[] collection = it.next();
            Map<String, Object> parameter = (Map<String, Object>) collection[0];
            if ( collectionName.equals( parameter.get( "name" ) ) )
                return collection;
        }
        return null;
    }

}