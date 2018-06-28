package org.opengis.cite.wfs30.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinkByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.wfs30.util.JsonUtils.hasProperty;
import static org.opengis.cite.wfs30.util.JsonUtils.linkIncludesRelAndType;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class JsonUtilsTest {

    private static JsonPath jsonPath;

    @BeforeClass
    public static void parseJson() {
        InputStream json = JsonUtilsTest.class.getResourceAsStream( "../collections/collections.json" );
        jsonPath = new JsonPath( json );
    }

    @Test
    public void testFindLinkToItself() {
        List<Map<String, Object>> links = jsonPath.getList( "links" );
        Map<String, Object> linkToItself = findLinkByRel( links, "self" );

        assertThat( linkToItself.get( "href" ),
                    is( "http://www.ldproxy.nrw.de/rest/services/kataster/collections/?f=json" ) );
        assertThat( linkToItself.get( "rel" ), is( "self" ) );
        assertThat( linkToItself.get( "type" ), is( "application/json" ) );
        assertThat( linkToItself.get( "title" ), is( "this document" ) );
    }

    @Test
    public void testLinkIncludesRelAndType() {
        List<Map<String, Object>> links = jsonPath.getList( "links" );
        Map<String, Object> linkToItself = findLinkByRel( links, "self" );
        boolean includesRelAndType = linkIncludesRelAndType( linkToItself );

        assertThat( includesRelAndType, is( true ) );
    }

    @Test
    public void testFindLinksWithoutRelOrType() {
        List<Map<String, Object>> links = jsonPath.getList( "links" );
        List<String> linksWithoutRelOrType = findLinksWithoutRelOrType( links );

        assertThat( linksWithoutRelOrType.size(), is( 0 ) );
    }

    @Test
    public void testFindLinksWithSupportedMediaTypeByRel() {
        List<Map<String, Object>> links = jsonPath.getList( "links" );
        List<String> mediaTypes = Arrays.asList( "text/html", "application/json" );
        List<Map<String, Object>> linksWithMediaTypes = findLinksWithSupportedMediaTypeByRel( links, mediaTypes,
                                                                                              "alternate" );

        assertThat( linksWithMediaTypes.size(), is( 1 ) );
    }

    @Test
    public void testHasProperty_true() {
        boolean hasProperty = hasProperty( "links", jsonPath );
        assertThat( hasProperty, is( true ) );
    }

    @Test
    public void testHasProperty_false() {
        boolean hasProperty = hasProperty( "doesNotExist", jsonPath );
        assertThat( hasProperty, is( false ) );
    }

}
