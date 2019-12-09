package org.opengis.cite.ogcapifeatures10.openapi3;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UrlBuilderTest {

    @Test
    public void testBuildUrl_collectionMetadata_withTemplate() {
        TestPoint tp = new TestPoint( "http://localhost:8080/service", "/collections/{name}", Collections.emptyMap() );
        String url = new UriBuilder( tp ).collectionName( "water" ).buildUrl();

        assertThat( url, is( "http://localhost:8080/service/collections/water" ) );
    }

    @Test
    public void testBuildUrl_collectionMetadata_withoutTemplate() {
        TestPoint tp = new TestPoint( "http://localhost:8080/service", "/collections/forest", Collections.emptyMap() );
        String url = new UriBuilder( tp ).collectionName( "forest" ).buildUrl();

        assertThat( url, is( "http://localhost:8080/service/collections/forest" ) );
    }

    @Test
    public void testBuildUrl_collection_withTemplate() {
        TestPoint tp = new TestPoint( "http://localhost:8080/service", "/collections/{name}/items",
                                      Collections.emptyMap() );
        String url = new UriBuilder( tp ).collectionName( "water" ).buildUrl();

        assertThat( url, is( "http://localhost:8080/service/collections/water/items" ) );
    }

    @Test
    public void testBuildUrl_collection_withoutTemplate() {
        TestPoint tp = new TestPoint( "http://localhost:8080/service", "/collections/forest/items",
                                      Collections.emptyMap() );
        String url = new UriBuilder( tp ).collectionName( "forest" ).buildUrl();

        assertThat( url, is( "http://localhost:8080/service/collections/forest/items" ) );
    }

    @Test
    public void testBuildUrl_feature_withFeatureIdTemplate() {
        TestPoint tp = new TestPoint( "http://localhost:8080/service", "/collections/forest/items/{featureId}",
                                      Collections.emptyMap() );
        String url = new UriBuilder( tp ).collectionName( "forest" ).featureId( "1" ).buildUrl();

        assertThat( url, is( "http://localhost:8080/service/collections/forest/items/1" ) );
    }

    @Test
    public void testBuildUrl_feature_withCollectionNameAndFeatureIdTemplate() {
        TestPoint tp = new TestPoint( "http://localhost:8080/service", "/collections/{name}/items/{featureId}",
                                      Collections.emptyMap() );
        String url = new UriBuilder( tp ).collectionName( "water" ).featureId( "2" ).buildUrl();

        assertThat( url, is( "http://localhost:8080/service/collections/water/items/2" ) );
    }

    @Test
    public void testBuildUrl_feature_withoutTemplates() {
        TestPoint tp = new TestPoint( "http://localhost:8080/service", "/collections/forest/items/3",
                                      Collections.emptyMap() );
        String url = new UriBuilder( tp ).collectionName( "forest" ).featureId( "3" ).buildUrl();

        assertThat( url, is( "http://localhost:8080/service/collections/forest/items/3" ) );
    }
}
