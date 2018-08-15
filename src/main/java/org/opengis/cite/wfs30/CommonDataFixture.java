package org.opengis.cite.wfs30;

import static org.opengis.cite.wfs30.SuiteAttribute.REQUIREMENTCLASSES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opengis.cite.wfs30.conformance.RequirementClass;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CommonDataFixture extends CommonFixture {

    private List<RequirementClass> requirementClasses;

    @BeforeClass
    public void requirementClasses( ITestContext testContext ) {
        this.requirementClasses = (List<RequirementClass>) testContext.getSuite().getAttribute( REQUIREMENTCLASSES.getName() );
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
