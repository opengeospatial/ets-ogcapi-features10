package org.opengis.cite.wfs30.util;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.wfs30.util.JsonUtils.collectNumberOfAllReturnedFeatures;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinkByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithSupportedMediaTypeByRel;
import static org.opengis.cite.wfs30.util.JsonUtils.findLinksWithoutRelOrType;
import static org.opengis.cite.wfs30.util.JsonUtils.formatDate;
import static org.opengis.cite.wfs30.util.JsonUtils.formatDateRange;
import static org.opengis.cite.wfs30.util.JsonUtils.formatDateRangeWithDuration;
import static org.opengis.cite.wfs30.util.JsonUtils.hasProperty;
import static org.opengis.cite.wfs30.util.JsonUtils.linkIncludesRelAndType;
import static org.opengis.cite.wfs30.util.JsonUtils.parseAsDate;
import static org.opengis.cite.wfs30.util.JsonUtils.parseFeatureId;
import static org.opengis.cite.wfs30.util.JsonUtils.parseSpatialExtent;
import static org.opengis.cite.wfs30.util.JsonUtils.parseTemporalExtent;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class JsonUtilsTest {

    private static JsonPath jsonCollection;

    private static JsonPath jsonCollectionItem;

    @BeforeClass
    public static void parseJson() {
        InputStream collectionJson = JsonUtilsTest.class.getResourceAsStream( "../collections/collections.json" );
        jsonCollection = new JsonPath( collectionJson );
        InputStream collectionItemsJson = JsonUtilsTest.class.getResourceAsStream( "../collections/collectionItems.json" );
        jsonCollectionItem = new JsonPath( collectionItemsJson );
    }

    @Before
    public void setUp() {
        initJadlerListeningOn( 8090 );
    }

    @After
    public void tearDown() {
        closeJadler();
    }

    @Test
    public void testParseFeatureId() {
        String featureId = parseFeatureId( jsonCollectionItem );
        assertThat( featureId, is( "DENW19AL0000geMFFL" ) );
    }

    @Test
    public void testParseAsDate() {
        String timeStamp = "2017-03-04T01:02:33Z";
        ZonedDateTime dateTime = parseAsDate( timeStamp );

        assertThat( dateTime.getYear(), is( 2017 ) );
        assertThat( dateTime.getMonth(), is( Month.MARCH ) );
        assertThat( dateTime.getDayOfMonth(), is( 4 ) );
        assertThat( dateTime.getHour(), is( 1 ) );
        assertThat( dateTime.getMinute(), is( 2 ) );
        assertThat( dateTime.getSecond(), is( 33 ) );
    }

    @Test
    public void testFormatDate() {
        String dateTime = "2017-03-04T01:02:33Z";
        ZonedDateTime dateTimeAsZonedDateTime = parseAsDate( dateTime );

        String dateTimeAsString = formatDate( dateTimeAsZonedDateTime );
        assertThat( dateTimeAsString, is( dateTime ) );

        String dateAsString = formatDate( dateTimeAsZonedDateTime.toLocalDate() );
        assertThat( dateAsString, is( "2017-03-04" ) );
    }

    @Test
    public void testFormatDateRange() {
        String beginDateTime = "2017-03-04T01:02:33Z";
        ZonedDateTime begin = parseAsDate( beginDateTime );
        String endDateTime = "2018-03-04T01:02:33Z";
        ZonedDateTime end = parseAsDate( endDateTime );

        String asString = formatDateRange( begin, end );
        assertThat( asString, is( "2017-03-04T01:02:33Z/2018-03-04T01:02:33Z" ) );
    }

    @Test
    public void testFormatDateRangeWithDuration() {
        String beginDate = "2017-03-04";
        LocalDate begin = LocalDate.parse( beginDate );
        String endDate = "2017-04-06";
        LocalDate end = LocalDate.parse( endDate );

        String asString = formatDateRangeWithDuration( begin, end );
        assertThat( asString, is( "2017-03-04/P1M2D" ) );
    }

    @Test
    public void testTemporalExtent() {
        List<Object> collections = jsonCollection.getList( "collections" );
        TemporalExtent extent = parseTemporalExtent( (Map<String, Object>) collections.get( 0 ) );

        ZonedDateTime begin = extent.getBegin();
        ZonedDateTime end = extent.getEnd();
        assertThat( begin, is( ZonedDateTime.parse( "2017-01-01T00:00:00Z" ) ) );
        assertThat( end, is( ZonedDateTime.parse( "2017-12-31T23:59:59Z" ) ) );
    }

    @Test
    public void testParseSpatialExtent() {
        List<Object> collections = jsonCollection.getList( "collections" );
        BBox extent = parseSpatialExtent( (Map<String, Object>) collections.get( 0 ) );

        String queryParam = extent.asQueryParameter();
        String[] queryParams = queryParam.split( "," );
        assertThat( queryParams.length, is( 4 ) );
        assertEquals( Double.parseDouble( queryParams[0] ), 5.61272621360749, 0.00001 );
        assertEquals( Double.parseDouble( queryParams[1] ), 50.2373512077239, 0.00001 );
        assertEquals( Double.parseDouble( queryParams[2] ), 9.58963433710139, 0.00001 );
        assertEquals( Double.parseDouble( queryParams[3] ), 52.5286304537795, 0.00001 );
    }

    @Test
    public void testFindLinkToItself() {
        List<Map<String, Object>> links = jsonCollection.getList( "links" );
        Map<String, Object> linkToItself = findLinkByRel( links, "self" );

        assertThat( linkToItself.get( "href" ),
                    is( "http://www.ldproxy.nrw.de/rest/services/kataster/collections/?f=json" ) );
        assertThat( linkToItself.get( "rel" ), is( "self" ) );
        assertThat( linkToItself.get( "type" ), is( "application/json" ) );
        assertThat( linkToItself.get( "title" ), is( "this document" ) );
    }

    @Test
    public void testLinkIncludesRelAndType() {
        List<Map<String, Object>> links = jsonCollection.getList( "links" );
        Map<String, Object> linkToItself = findLinkByRel( links, "self" );
        boolean includesRelAndType = linkIncludesRelAndType( linkToItself );

        assertThat( includesRelAndType, is( true ) );
    }

    @Test
    public void testFindLinksWithoutRelOrType() {
        List<Map<String, Object>> links = jsonCollection.getList( "links" );
        List<String> linksWithoutRelOrType = findLinksWithoutRelOrType( links );

        assertThat( linksWithoutRelOrType.size(), is( 0 ) );
    }

    @Test
    public void testFindLinksWithSupportedMediaTypeByRel() {
        List<Map<String, Object>> links = jsonCollection.getList( "links" );
        List<String> mediaTypes = Arrays.asList( "text/html", "application/json" );
        List<Map<String, Object>> linksWithMediaTypes = findLinksWithSupportedMediaTypeByRel( links, mediaTypes,
                                                                                              "alternate" );

        assertThat( linksWithMediaTypes.size(), is( 1 ) );
    }

    @Test
    public void testHasProperty_true() {
        boolean hasProperty = hasProperty( "links", jsonCollection );
        assertThat( hasProperty, is( true ) );
    }

    @Test
    public void testHasProperty_false() {
        boolean hasProperty = hasProperty( "doesNotExist", jsonCollection );
        assertThat( hasProperty, is( false ) );
    }

    @Test
    public void testCollectNumberOfAllReturnedFeatures()
                            throws Exception {
        prepareJadler();
        URL json = new URL( "http://localhost:8090/collections/lakes/items" );
        JsonPath jsonPath = new JsonPath( json );

        int numberOfAllFeatures = collectNumberOfAllReturnedFeatures( jsonPath, -1 );

        assertThat( numberOfAllFeatures, is( 25 ) );
    }

    private void prepareJadler() {
        InputStream item1_10 = getClass().getResourceAsStream( "items_1-10.json" );
        onRequest().havingParameter( "startindex", nullValue() ).respond().withBody( item1_10 );

        InputStream item11_20 = getClass().getResourceAsStream( "items_11-20.json" );
        onRequest().havingParameterEqualTo( "startindex", "10" ).respond().withBody( item11_20 );

        InputStream item21_30 = getClass().getResourceAsStream( "items_21-30.json" );
        onRequest().havingParameterEqualTo( "startindex", "20" ).respond().withBody( item21_30 );

        InputStream item31_40 = getClass().getResourceAsStream( "items_31-40.json" );
        onRequest().havingParameter( "startindex",
                                     allOf( notNullValue(), not( hasItems( "10" ) ), not( hasItems( "20" ) ) ) ).respond().withBody( item31_40 );
    }

}
