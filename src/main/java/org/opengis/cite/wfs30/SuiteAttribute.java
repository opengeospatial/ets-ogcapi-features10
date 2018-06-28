package org.opengis.cite.wfs30;

import java.io.File;
import java.net.URI;
import java.util.List;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.sun.jersey.api.client.Client;

/**
 * An enumerated type defining ISuite attributes that may be set to constitute a shared test fixture.
 */
@SuppressWarnings("rawtypes")
public enum SuiteAttribute {

    /**
     * A client component for interacting with HTTP endpoints.
     */
    CLIENT( "httpClient", Client.class ),

    /**
     * The root URL.
     */
    IUT( "instanceUnderTest", URI.class ),

    /**
     * A File containing the test subject or a description of it.
     */
    TEST_SUBJ_FILE( "testSubjectFile", File.class ),

    /**
     * Parsed OpenApi3 document resource /api; Added during execution.
     */
    API_MODEL( "apiModel", OpenApi3.class ),

    /**
     * Parsed collections from resource /collections; Added during execution.
     */
    COLLECTIONS( "collections", List.class );

    private final Class attrType;

    private final String attrName;

    SuiteAttribute( String attrName, Class attrType ) {
        this.attrName = attrName;
        this.attrType = attrType;
    }

    public Class getType() {
        return attrType;
    }

    public String getName() {
        return attrName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( attrName );
        sb.append( '(' ).append( attrType.getName() ).append( ')' );
        return sb.toString();
    }
}
