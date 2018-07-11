package org.opengis.cite.wfs30.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.wfs30.util.JsonUtils.collectNumberOfAllReturnedFeatures;

import java.net.URL;

import org.junit.Test;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class JsonUtilsIT {

    @Test
    public void testCollectNumberOfAllReturnedFeatures()
                            throws Exception {
        URL json = new URL( "http://geo.kralidis.ca/pygeoapi/collections/lakes/items" );
        JsonPath jsonPath = new JsonPath( json );

        int numberOfAllFeatures = collectNumberOfAllReturnedFeatures( jsonPath );

        assertThat( numberOfAllFeatures, is( 25 ) );
    }

}
