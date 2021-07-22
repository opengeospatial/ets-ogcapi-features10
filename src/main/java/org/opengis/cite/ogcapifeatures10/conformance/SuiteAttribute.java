package org.opengis.cite.ogcapifeatures10.conformance;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

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
     * The number of collections to test.
     */
    NO_OF_COLLECTIONS( "noOfCollections", Integer.class ),

    /**
     * Parsed OpenApi3 document resource /api; Added during execution.
     */
    API_MODEL( "apiModel", OpenApi3.class ),

    /**
     * Requirement classes parsed from /conformance; Added during execution.
     */
    REQUIREMENTCLASSES( "requirementclasses", List.class ),

    /**
     * Response of resource /collections; Added during execution.
     */
    COLLECTIONS_RESPONSE( "collectionsResponse", Map.class ),

    /**
     * Parsed collections from resource /collections; Added during execution.
     */
    COLLECTIONS( "collections", List.class ),

    /**
     * collectionId mapped to response of resource /collection/{collectionId}; Added during execution.
     */
    COLLECTION_BY_ID( "collectionById", Map.class ),

    /**
     * collectionId mapped to parsed CRSs of resource /collection/{collectionId}; Added during execution.
     */
    COLLECTION_CRS_BY_ID( "collectionCrsById", Map.class ),

    /**
     * collectionId mapped to parsed default CRS of resource /collection/{collectionId}; Added during execution.
     */
    COLLECTION_DEFAULT_CRS_BY_ID( "collectionDefaultCrsById", Map.class ),

    /**
     * Collection names assigned to a feature id parsed from resource /collections/{name}/items; Added during execution.
     */
    FEATUREIDS( "featureIds", Map.class );

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
