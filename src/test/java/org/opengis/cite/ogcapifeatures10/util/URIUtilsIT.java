package org.opengis.cite.ogcapifeatures10.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies the behavior of the URIUtils class.
 */
public class URIUtilsIT {

	@Test
	public void resolveHttpUriAsFile() throws IOException {
		URI uriRef = URI.create("http://www.w3schools.com/xml/note.xml");
		File file = URIUtils.dereferenceURI(uriRef);
		Assert.assertNotNull(file);
		Assert.assertTrue("File should not be empty", file.length() > 0);
	}

}
