package org.opengis.cite.ogcapifeatures10.conformance.core.collections;

import static org.opengis.cite.ogcapifeatures10.EtsAssert.assertTrue;
import static org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils.retrieveTestPointsForCollection;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.collectNumberOfAllReturnedFeatures;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.formatDate;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.hasProperty;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseAsDate;
import static org.opengis.cite.ogcapifeatures10.util.JsonUtils.parseAsList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;

import org.opengis.cite.ogcapifeatures10.openapi3.OpenApiUtils;
import org.opengis.cite.ogcapifeatures10.openapi3.TestPoint;
import org.testng.SkipException;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.model3.Parameter;

import io.restassured.path.json.JsonPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeaturesAssertions {

    static void assertIntegerGreaterZero( Object value, String propertyName ) {
        if ( value instanceof Number )
            assertIntegerGreaterZero( ( (Number) value ).intValue(), propertyName );
        else if ( value instanceof String )
            try {
                int valueAsInt = Integer.parseInt( (String) value );
                assertIntegerGreaterZero( valueAsInt, propertyName );
            } catch ( NumberFormatException e ) {
                String msg = "Expected property '%s' to be an integer, but was '%s'";
                throw new AssertionError( String.format( msg, propertyName, value ) );
            }
    }

    static void assertIntegerGreaterZero( int value, String propertyName ) {
        String msg = "Expected property '%s' to be an integer greater than 0, but was '%s'";
        assertTrue( value > 0, String.format( msg, propertyName, value ) );
    }

    static void assertTimeStamp( String collectionName, JsonPath jsonPath, ZonedDateTime timeStampBeforeResponse,
                                 ZonedDateTime timeStampAfterResponse, boolean skipIfNoTimeStamp ) {
        String timeStamp = jsonPath.getString( "timeStamp" );
        if ( timeStamp == null )
            if ( skipIfNoTimeStamp )
                throw new SkipException( "Property timeStamp is not set in collection items '" + collectionName + "'" );
            else
                return;

        ZonedDateTime date = parseAsDate( timeStamp );
        assertNotNull(date, "Not valid timestamp.");
    }

    static void assertNumberReturned( String collectionName, JsonPath jsonPath, boolean skipIfNoNumberReturned ) {
        if ( !hasProperty( "numberReturned", jsonPath ) )
            if ( skipIfNoNumberReturned )
                throw new SkipException( "Property numberReturned is not set in collection items '" + collectionName
                                         + "'" );
            else
                return;

        int numberReturned = jsonPath.getInt( "numberReturned" );
        int numberOfFeatures = parseAsList( "features", jsonPath ).size();
        assertEquals( numberReturned, numberOfFeatures,
                      "Value of numberReturned (" + numberReturned
                                                        + ") does not match the number of features in the response ("
                                                        + numberOfFeatures + ")" );
    }

    static void assertNumberMatched( OpenApi3 apiModel, URI iut, String collectionName, JsonPath jsonPath,
                                     boolean skipIfNoNumberMatched )
                            throws URISyntaxException {
        if ( !hasProperty( "numberMatched", jsonPath ) )
            if ( skipIfNoNumberMatched )
                throw new SkipException( "Property numberMatched is not set in collection items '" + collectionName
                                         + "'" );
            else
                return;

        int maximumLimit = -1;

        List<TestPoint> testPoints = retrieveTestPointsForCollection( apiModel, iut, collectionName );
        if ( !testPoints.isEmpty() ) {
            TestPoint testPoint = testPoints.get( 0 );
            Parameter limitParameter = OpenApiUtils.retrieveParameterByName( testPoint.getPath(), apiModel, "limit" );
            if ( limitParameter != null && limitParameter.getSchema() != null ) {
                maximumLimit = limitParameter.getSchema().getMaximum().intValue();
            }
        }
        int numberMatched = jsonPath.getInt( "numberMatched" );
        int numberOfAllReturnedFeatures = collectNumberOfAllReturnedFeatures( jsonPath, maximumLimit );
        assertEquals( numberMatched, numberOfAllReturnedFeatures,
                      "Value of numberReturned (" + numberMatched + ") does not match the number of features in all responses ("
                                                                  + numberOfAllReturnedFeatures + ")" );
    }

}
