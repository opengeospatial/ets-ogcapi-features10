package org.opengis.cite.wfs30.openapi3;

import java.util.List;
import java.util.Map;

import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.sun.jersey.api.uri.UriTemplate;

/**
 * Encapsulates a Test Point with the UriTemplate and predefined replacements.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class TestPoint {

    private UriTemplate uriTemplate;

    private Map<String, String> templateReplacement;

    private List<String> requirementClasses;

    private Map<String, MediaType> contentMediaTypes;

    /**
     * Instantiates a TestPoint with UriTemplate but without defined replacements.
     * 
     * @param uriTemplate
     *            never <code>null</code>
     * @param contentMediaTypes
     *            the content media types for the GET operation with response "200", may be <code>null</code>
     */
    public TestPoint( UriTemplate uriTemplate, Map<String, MediaType> contentMediaTypes ) {
        this( uriTemplate, null, contentMediaTypes );
    }

    /**
     * Instantiates a TestPoint with UriTemplate and predefined replacements.
     *
     * @param uriTemplate
     *            never <code>null</code>
     * @param templateReplacement
     *            may be <code>null</code>
     * @param contentMediaTypes
     *            the content media types for the GET operation with response "200", may be <code>null</code>
     */
    public TestPoint( UriTemplate uriTemplate, Map<String, String> templateReplacement,
                      Map<String, MediaType> contentMediaTypes ) {
        this.uriTemplate = uriTemplate;
        this.templateReplacement = templateReplacement;
        this.contentMediaTypes = contentMediaTypes;
    }

    /**
     * @return the UriTemplate, never <code>null</code>
     */
    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    /**
     * @return predefined replacements, may be <code>null</code>
     */
    public Map<String, String> getTemplateReplacement() {
        return templateReplacement;
    }

    /**
     * @return a list of requirement classes the server conforms to, <code>null</code> if the conformance classes are
     *         not requested
     */
    public List<String> getRequirementClasses() {
        return requirementClasses;
    }

    /**
     * Adds the requirement classes the server conforms to
     * 
     * @param requirementClasses
     *            never <code>null</code>
     */
    public void addRequirementClasses( List<String> requirementClasses ) {
        this.requirementClasses = requirementClasses;
    }

    /**
     * @return the content media types for the GET operation with response "200", may be <code>null</code>
     */
    public Map<String, MediaType> getContentMediaTypes() {
        return contentMediaTypes;
    }

    /**
     * Creates an URI from the template with the replacement.
     * 
     * @return the URI created from the template, never <code>null</code>
     */
    public String createUri() {
        return uriTemplate.createURI( templateReplacement );
    }

    @Override
    public String toString() {
        return "Pattern: " + uriTemplate.getPattern() + ", Replacements: " + templateReplacement;
    }
}
