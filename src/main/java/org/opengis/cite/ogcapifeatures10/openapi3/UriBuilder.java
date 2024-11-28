package org.opengis.cite.ogcapifeatures10.openapi3;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.uri.UriTemplate;
import org.glassfish.jersey.uri.internal.UriPart;
import org.glassfish.jersey.uri.internal.UriTemplateParser;

/**
 * Builds a URL out of a TestPoint.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UriBuilder {

	private final TestPoint testPoint;

	private final Map<String, String> templateReplacements = new HashMap<>();

	/**
	 * <p>
	 * Constructor for UriBuilder.
	 * </p>
	 * @param testPoint never <code>null</code>
	 */
	public UriBuilder(TestPoint testPoint) {
		this.testPoint = testPoint;
		this.templateReplacements.putAll(testPoint.getPredefinedTemplateReplacement());
	}

	/**
	 * Adds the collectionName to the URI
	 * @param collectionName never <code>null</code>
	 * @return this UrlBuilder
	 */
	public UriBuilder collectionName(String collectionName) {
		String templateName = retrieveCollectionNameTemplateName();
		addTemplateReplacement(collectionName, templateName);
		return this;
	}

	/**
	 * Adds the featureId to the URI
	 * @param featureId never <code>null</code>
	 * @return this UrlBuilder
	 */
	public UriBuilder featureId(String featureId) {
		String templateName = retrieveFeatureIdTemplateName();
		addTemplateReplacement(featureId, templateName);
		return this;
	}

	/**
	 * <p>
	 * buildUrl.
	 * </p>
	 * @return this URI, never <code>null</code>
	 */
	public String buildUrl() {
		UriTemplate uriTemplate = new UriTemplate(testPoint.getServerUrl() + testPoint.getPath());
		return uriTemplate.createURI(templateReplacements);
	}

	private void addTemplateReplacement(String collectionName, String templateName) {
		if (templateName != null)
			templateReplacements.put(templateName, collectionName);
	}

	private String retrieveCollectionNameTemplateName() {
		String path = testPoint.getPath();
		UriTemplateParser uriTemplateParser = new UriTemplateParser(path);
		for (UriPart templateName : uriTemplateParser.getNames()) {
			if (path.contains("/collections/{" + templateName.getPart() + "}"))
				return templateName.getPart();
		}
		return null;
	}

	private String retrieveFeatureIdTemplateName() {
		String path = testPoint.getPath();
		UriTemplateParser uriTemplateParser = new UriTemplateParser(path);
		for (UriPart templateName : uriTemplateParser.getNames()) {
			if (path.endsWith("items/{" + templateName.getPart() + "}"))
				return templateName.getPart();
		}
		return null;
	}

}
