package org.opengis.cite.ogcapifeatures10.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UrnValidatorTest {

    @Test
    public void testIsValid_validUrn() {
        UrnValidator urnValidator = new UrnValidator();
        assertThat( urnValidator.isValid( "urn:example:1/406/47452/2" ), is( true ) );
    }

    @Test
    public void testIsValid_invalidUrn() {
        UrnValidator urnValidator = new UrnValidator();
        assertThat( urnValidator.isValid( "urn" ), is( false ) );
    }

    @Test
    public void testIsValid_null() {
        UrnValidator urnValidator = new UrnValidator();
        assertThat( urnValidator.isValid( null ), is( false ) );
    }

    @Test
    public void testIsValid_empty() {
        UrnValidator urnValidator = new UrnValidator();
        assertThat( urnValidator.isValid( "" ), is( false ) );
    }
}
