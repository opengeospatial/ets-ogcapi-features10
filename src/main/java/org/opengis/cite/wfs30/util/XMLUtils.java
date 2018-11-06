package org.opengis.cite.wfs30.util;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

/**
 * Provides various utility methods for accessing or manipulating XML representations.
 */
public class XMLUtils {

    /**
     * Writes the content of a DOM Node to a string. The XML declaration is omitted and the character encoding is set to
     * "US-ASCII" (any character outside of this set is serialized as a numeric character reference).
     *
     * @param node
     *            The DOM Node to be serialized.
     * @return A String representing the content of the given node.
     */
    public static String writeNodeToString( Node node ) {
        if ( null == node ) {
            return "";
        }
        Writer writer = null;
        try {
            Transformer idTransformer = TransformerFactory.newInstance().newTransformer();
            Properties outProps = new Properties();
            outProps.setProperty( OutputKeys.ENCODING, "US-ASCII" );
            outProps.setProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            outProps.setProperty( OutputKeys.INDENT, "yes" );
            idTransformer.setOutputProperties( outProps );
            writer = new StringWriter();
            idTransformer.transform( new DOMSource( node ), new StreamResult( writer ) );
        } catch ( TransformerException ex ) {
            TestSuiteLogger.log( Level.WARNING, "Failed to serialize node " + node.getNodeName(), ex );
        }
        return writer.toString();
    }

}
