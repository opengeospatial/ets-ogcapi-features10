package org.opengis.cite.ogcapifeatures10.openapi3;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPoints;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForApi;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollection;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollectionMetadata;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollections;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollectionsMetadata;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForFeature;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class OpenApiUtilsTest {

    private static URI iut;

    @BeforeClass
    public static void instantiateUri()
                            throws URISyntaxException {
        iut = new URI( "http://localhost:8080/oaf" );
    }

    @Test
    public void testRetrieveTestPoints()
                            throws URISyntaxException {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPoints( apiModel, iut );

        assertThat( testPoints.size(), is( 3 ) );

    }

    @Ignore
    @Test
    public void testRetrieveTestPoints_moreComplex() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi_moreComplex.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPoints( apiModel, iut );

        assertThat( testPoints.size(), is( 4 ) );

        TestPoint testPointWIthIndex = testPoints.get( 0 );
        assertThat( testPointWIthIndex.getPredefinedTemplateReplacement().size(), is( 1 ) );
        assertThat( testPointWIthIndex.getPredefinedTemplateReplacement().get( "index" ), is( "10" ) );

        TestPoint testPointWIthIndexAndEnum1 = testPoints.get( 1 );
        assertThat( testPointWIthIndexAndEnum1.getPredefinedTemplateReplacement().size(), is( 2 ) );
        assertThat( testPointWIthIndexAndEnum1.getPredefinedTemplateReplacement().get( "index" ), is( "10" ) );
        assertThat( testPointWIthIndexAndEnum1.getPredefinedTemplateReplacement().get( "enum" ), is( "eins" ) );

        TestPoint testPointWIthIndexAndEnum2 = testPoints.get( 2 );
        assertThat( testPointWIthIndexAndEnum2.getPredefinedTemplateReplacement().size(), is( 2 ) );
        assertThat( testPointWIthIndexAndEnum2.getPredefinedTemplateReplacement().get( "index" ), is( "10" ) );
        assertThat( testPointWIthIndexAndEnum2.getPredefinedTemplateReplacement().get( "enum" ), is( "zwei" ) );

        TestPoint testPointWIthIndexAndEnum3 = testPoints.get( 3 );
        assertThat( testPointWIthIndexAndEnum3.getPredefinedTemplateReplacement().size(), is( 2 ) );
        assertThat( testPointWIthIndexAndEnum3.getPredefinedTemplateReplacement().get( "index" ), is( "10" ) );
        assertThat( testPointWIthIndexAndEnum3.getPredefinedTemplateReplacement().get( "enum" ), is( "drei" ) );
    }

    @Test
    public void testRetrieveTestPoints_API() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForApi( apiModel, iut );

        assertThat( testPoints.size(), is( 1 ) );
    }

    @Test
    public void testRetrieveTestPoints_COLLECTIONS() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollectionsMetadata( apiModel, iut );

        assertThat( testPoints.size(), is( 1 ) );
        Map<String, MediaType> contentMediaTypes = testPoints.get( 0 ).getContentMediaTypes();
        assertThat( contentMediaTypes.size(), is( 2 ) );
    }

    @Test
    public void testRetrieveTestPointsForCollectionMetadata() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollectionMetadata( apiModel, iut, "flurstueck" );

        assertThat( testPoints.size(), is( 1 ) );

        TestPoint testPoint = testPoints.get( 0 );
        assertThat( testPoint.getServerUrl(), is( "http://localhost:8090/rest/services/kataster" ) );
        assertThat( testPoint.getPath(), is( "/collections/flurstueck" ) );

        Map<String, MediaType> contentMediaTypes = testPoint.getContentMediaTypes();
        assertThat( contentMediaTypes.size(), is( 2 ) );
    }

    @Test
    public void testRetrieveTestPointsForCollection() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollection( apiModel, iut, "flurstueck" );

        assertThat( testPoints.size(), is( 1 ) );

        TestPoint testPoint = testPoints.get( 0 );
        assertThat( testPoint.getServerUrl(), is( "http://localhost:8090/rest/services/kataster" ) );
        assertThat( testPoint.getPath(), is( "/collections/flurstueck/items" ) );

        Map<String, MediaType> contentMediaTypes = testPoint.getContentMediaTypes();
        assertThat( contentMediaTypes.size(), is( 2 ) );
    }

    @Test
    public void testRetrieveTestPointsForFeature() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForFeature( apiModel, iut, "flurstueck", "abc" );

        assertThat( testPoints.size(), is( 1 ) );

        TestPoint testPoint = testPoints.get( 0 );
        assertThat( testPoint.getServerUrl(), is( "http://localhost:8090/rest/services/kataster" ) );
        assertThat( testPoint.getPath(), is( "/collections/flurstueck/items/{featureId}" ) );
        Map<String, MediaType> contentMediaTypes = testPoint.getContentMediaTypes();
        assertThat( contentMediaTypes.size(), is( 2 ) );
    }

    @Test
    public void testRetrieveTestPointsForCollections_all() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollections( apiModel, iut, -1 );

        assertThat( testPoints.size(), is( 3 ) );
        List<String> paths = testPoints.stream().map( tp -> tp.getPath() ).collect( Collectors.toCollection( ArrayList::new ) );
        assertThat( paths, hasItem( "/collections/flurstueck/items" ) );
        assertThat( paths, hasItem( "/collections/gebaeudebauwerk/items" ) );
        assertThat( paths, hasItem( "/collections/verwaltungseinheit/items" ) );
    }

    @Test
    public void testRetrieveTestPointsForCollections_limit1() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollections( apiModel, iut, 1 );

        assertThat( testPoints.size(), is( 1 ) );
        List<String> paths = testPoints.stream().map( tp -> tp.getPath() ).collect( Collectors.toCollection( ArrayList::new ) );
        assertThat( paths, hasItem( "/collections/flurstueck/items" ) );
    }

    @Test
    public void testRetrieveTestPointsForCollections_limitGreaterThanSize() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollections( apiModel, iut, 6 );

        assertThat( testPoints.size(), is( 3 ) );
        List<String> paths = testPoints.stream().map( tp -> tp.getPath() ).collect( Collectors.toCollection( ArrayList::new ) );
        assertThat( paths, hasItem( "/collections/flurstueck/items" ) );
    }

    @Test
    public void testRetrieveTestPoints_COLLECTIONS_compactAPI() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi_compact-api.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollectionsMetadata( apiModel, iut );

        assertThat( testPoints.size(), is( 1 ) );

        TestPoint testPoint = testPoints.get( 0 );
        // assertThat( testPoint.createUri(), is( "http://cloudsdi.geo-solutions.it:80/geoserver/wfs3/collections" ) );

        assertThat( testPoint.getServerUrl(), is( "http://cloudsdi.geo-solutions.it:80/geoserver/wfs3" ) );
        assertThat( testPoint.getPath(), is( "/collections" ) );

        assertThat( testPoint.getContentMediaTypes().size(), is( 4 ) );
    }

    @Test
    public void testRetrieveTestPointsForCollectionMetadata_compactAPI() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi_compact-api.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollectionMetadata( apiModel, iut, "test__countries" );

        assertThat( testPoints.size(), is( 1 ) );

        TestPoint testPoint = testPoints.get( 0 );

        assertThat( testPoint.getServerUrl(), is( "http://cloudsdi.geo-solutions.it:80/geoserver/wfs3" ) );
        assertThat( testPoint.getPath(), is( "/collections/{collectionId}" ) );
    }

    @Test
    public void testRetrieveTestPointsForCollection_compactAPI() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi_compact-api.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollection( apiModel, iut, "test__countries" );

        assertThat( testPoints.size(), is( 1 ) );

        TestPoint testPoint = testPoints.get( 0 );
        assertThat( testPoint.getServerUrl(), is( "http://cloudsdi.geo-solutions.it:80/geoserver/wfs3" ) );
        assertThat( testPoint.getPath(), is( "/collections/{collectionId}/items" ) );
    }

    @Test
    public void testRetrieveTestPointsForFeature_compactAPI() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi_compact-api.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForFeature( apiModel, iut, "test__countries", "abc" );

        assertThat( testPoints.size(), is( 1 ) );

        TestPoint testPoint = testPoints.get( 0 );
        assertThat( testPoint.getServerUrl(), is( "http://cloudsdi.geo-solutions.it:80/geoserver/wfs3" ) );
        assertThat( testPoint.getPath(), is( "/collections/{collectionId}/items/{featureId}" ) );
    }

    @Test
    public void testRetrieveTestPointsForCollections_all_compactAPI() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi_compact-api.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollections( apiModel, iut, -1 );

        assertThat( testPoints.size(), is( 118 ) );
        assertThat( testPoints.get( 0 ).getPath(), is( "/collections/{collectionId}/items" ) );
    }

    @Test
    public void testRetrieveTestPointsForCollections_limit1_compactAPI() {
        OpenApi3Parser parser = new OpenApi3Parser();

        URL openAppiDocument = OpenApiUtilsTest.class.getResource( "openapi_compact-api.json" );
        OpenApi3 apiModel = parser.parse( openAppiDocument, true );
        List<TestPoint> testPoints = retrieveTestPointsForCollections( apiModel, iut, 1 );

        assertThat( testPoints.size(), is( 1 ) );
        assertThat( testPoints.get( 0 ).getPath(), is( "/collections/{collectionId}/items" ) );
    }
}
