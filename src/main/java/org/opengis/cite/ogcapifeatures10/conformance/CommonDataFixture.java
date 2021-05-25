package org.opengis.cite.ogcapifeatures10.conformance;

import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.API_MODEL;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.NO_OF_COLLECTIONS;
import static org.opengis.cite.ogcapifeatures10.conformance.SuiteAttribute.REQUIREMENTCLASSES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CommonDataFixture extends CommonFixture {

    private static final int DEFAULT_NUMBER_OF_COLLECTIONS = 3;

    private OpenApi3 apiModel;

    private List<RequirementClass> requirementClasses;

    protected int noOfCollections = DEFAULT_NUMBER_OF_COLLECTIONS;

    @BeforeClass
    public void requirementClasses( ITestContext testContext ) {
        this.requirementClasses = (List<RequirementClass>) testContext.getSuite().getAttribute( REQUIREMENTCLASSES.getName() );
    }

    @BeforeClass
    public void noOfCollections( ITestContext testContext ) {
        Object noOfCollections = testContext.getSuite().getAttribute( NO_OF_COLLECTIONS.getName() );
        if ( noOfCollections != null ) {
            this.noOfCollections = (Integer) noOfCollections;
        }
    }

    @BeforeClass
    public void retrieveApiModel( ITestContext testContext ) {
        this.apiModel = (OpenApi3) testContext.getSuite().getAttribute( API_MODEL.getName() );
    }

    public OpenApi3 getApiModel() {
        if ( apiModel == null )
            throw new SkipException( "ApiModel is not available." );
        return apiModel;
    }

    protected List<String> createListOfMediaTypesToSupportForOtherResources( Map<String, Object> linkToSelf ) {
        if ( this.requirementClasses == null )
            throw new SkipException( "No requirement classes described in  resource /conformance available" );
        List<String> mediaTypesToSupport = new ArrayList<>();
        for ( RequirementClass requirementClass : this.requirementClasses )
            if ( requirementClass.hasMediaTypeForOtherResources() )
                mediaTypesToSupport.add( requirementClass.getMediaTypeOtherResources() );
        if ( linkToSelf != null )
            mediaTypesToSupport.remove( linkToSelf.get( "type" ) );
        return mediaTypesToSupport;
    }

    protected List<String> createListOfMediaTypesToSupportForFeatureCollectionsAndFeatures() {
        if ( this.requirementClasses == null )
            throw new SkipException( "No requirement classes described in  resource /conformance available" );
        List<String> mediaTypesToSupport = new ArrayList<>();
        for ( RequirementClass requirementClass : this.requirementClasses )
            if ( requirementClass.hasMediaTypeForFeaturesAndCollections() )
                mediaTypesToSupport.add( requirementClass.getMediaTypeFeaturesAndCollections() );
        return mediaTypesToSupport;
    }

    protected List<String> createListOfMediaTypesToSupportForFeatureCollectionsAndFeatures( Map<String, Object> linkToSelf ) {
        List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForFeatureCollectionsAndFeatures();
        if ( linkToSelf != null )
            mediaTypesToSupport.remove( linkToSelf.get( "type" ) );
        return mediaTypesToSupport;
    }
}
