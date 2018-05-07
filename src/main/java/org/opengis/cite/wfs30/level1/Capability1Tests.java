package org.opengis.cite.wfs30.level1;

import java.io.IOException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.opengis.cite.wfs30.CommonFixture;
import org.opengis.cite.wfs30.ErrorMessage;
import org.opengis.cite.wfs30.ErrorMessageKeys;
import org.opengis.cite.wfs30.SuiteAttribute;
import org.opengis.cite.validation.RelaxNGValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Includes various tests of capability 1.
 */
public class Capability1Tests extends CommonFixture {

    private Document testSubject;

    /**
     * Obtains the test subject from the ISuite context. The suite attribute
     * {@link org.opengis.cite.wfs30.SuiteAttribute#TEST_SUBJECT} should
     * evaluate to a DOM Document node.
     * 
     * @param testContext
     *            The test (group) context.
     */
    @BeforeClass
    public void obtainTestSubject(ITestContext testContext) {
        Object obj = testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        if ((null != obj) && Document.class.isAssignableFrom(obj.getClass())) {
            this.testSubject = Document.class.cast(obj);
        }
    }

    /**
     * Sets the test subject. This method is intended to facilitate unit
     * testing.
     *
     * @param testSubject A Document node representing the test subject or
     * metadata about it.
     */
    public void setTestSubject(Document testSubject) {
        this.testSubject = testSubject;
    }

    /**
     * Verifies the string is empty.
     */
    @Test(description = "Implements ATC 1-1")
    public void isEmpty() {
        String str = "  foo   ";
        Assert.assertTrue(str.isEmpty(),
                ErrorMessage.get(ErrorMessageKeys.EMPTY_STRING));
    }

    /**
     * Checks the behavior of the trim function.
     */
    @Test(description = "Implements ATC 1-2")
    public void trim() {
        String str = "  foo   ";
        Assert.assertTrue("foo".equals(str.trim()));
    }

    /**
     * Verify the test subject is a valid Atom feed.
     *
     * @throws SAXException
     *             If the resource cannot be parsed.
     * @throws IOException
     *             If the resource is not accessible.
     */
    @Test(description = "Implements ATC 1-3")
    public void docIsValidAtomFeed() throws SAXException, IOException {
        URL schemaRef = getClass().getResource(
                "/org/opengis/cite/wfs30/rnc/atom.rnc");
        RelaxNGValidator rngValidator = new RelaxNGValidator(schemaRef);
        Source xmlSource = (null != testSubject)
                ? new DOMSource(testSubject) : null;
        rngValidator.validate(xmlSource);
        ValidationErrorHandler err = rngValidator.getErrorHandler();
        Assert.assertFalse(err.errorsDetected(),
                ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
                err.getErrorCount(), err.toString()));
    }
}
