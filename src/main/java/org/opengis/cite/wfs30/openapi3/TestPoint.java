package org.opengis.cite.wfs30.openapi3;

import java.util.Map;

import com.sun.jersey.api.uri.UriTemplate;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class TestPoint {

    private UriTemplate uriTemplate;

    private Map<String, String> templateReplacement;

    public TestPoint( UriTemplate uriTemplate ) {
        this.uriTemplate = uriTemplate;
    }

    public TestPoint( UriTemplate uriTemplate, Map<String, String> templateReplacement ) {
        this.uriTemplate = uriTemplate;
        this.templateReplacement = templateReplacement;
    }

    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    public Map<String, String> getTemplateReplacement() {
        return templateReplacement;
    }

    @Override
    public String toString() {
        return "Pattern: " + uriTemplate.getPattern() + ", Replacements: " + templateReplacement;
    }
}
