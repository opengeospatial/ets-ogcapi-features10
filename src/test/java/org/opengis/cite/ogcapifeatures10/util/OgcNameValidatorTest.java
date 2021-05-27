package org.opengis.cite.ogcapifeatures10.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class OgcNameValidatorTest {

    @Test
    public void testIsValid_validUrn() {
        OgcNameValidator ogcNameValidator = new OgcNameValidator();
        assertThat( ogcNameValidator.isValid( "urn:ogc:def:crs:EPSG::3163" ), is( true ) );
    }

    @Test
    public void testIsValid_validUrnWitVersion() {
        OgcNameValidator ogcNameValidator = new OgcNameValidator();
        assertThat( ogcNameValidator.isValid( "urn:ogc:def:crs:OGC:1.3:CRS84" ), is( true ) );
    }

    @Test
    public void testIsValid_validHttp() {
        OgcNameValidator ogcNameValidator = new OgcNameValidator();
        assertThat( ogcNameValidator.isValid( "http://www.opengis.net/def/crs/EPSG/0/3163" ), is( true ) );
    }

    @Test
    public void testIsValid_invalidUrn() {
        OgcNameValidator ogcNameValidator = new OgcNameValidator();
        assertThat( ogcNameValidator.isValid( "urn:example:1/406/47452/2" ), is( false ) );
    }

    @Test
    public void testIsValid_null() {
        OgcNameValidator ogcNameValidator = new OgcNameValidator();
        assertThat( ogcNameValidator.isValid( null ), is( false ) );
    }

    @Test
    public void testIsValid_empty() {
        OgcNameValidator ogcNameValidator = new OgcNameValidator();
        assertThat( ogcNameValidator.isValid( "" ), is( false ) );
    }

}
