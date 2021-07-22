package org.opengis.cite.ogcapifeatures10.listener;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.MediaType;

import org.opengis.cite.ogcapifeatures10.conformance.CommonFixture;
import org.opengis.cite.ogcapifeatures10.util.ClientUtils;
import org.opengis.cite.ogcapifeatures10.util.XMLUtils;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.w3c.dom.Document;

/**
 * A listener that augments a test result with diagnostic information in the
 * event that a test method failed. This information will appear in the XML
 * report when the test run is completed.
 */
public class TestFailureListener extends TestListenerAdapter {

    /**
     * Sets the "request" and "response" attributes of a test result. The value
     * of these attributes is a string that contains information about the
     * content of an outgoing or incoming message: target resource, status code,
     * headers, entity (if present). The entity is represented as a String with
     * UTF-8 character encoding.
     *
     * @param result A description of a test result (with a fail verdict).
     */
    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);
        Object instance = result.getInstance();
        if ( CommonFixture.class.isInstance( instance)) {
            CommonFixture fixture = CommonFixture.class.cast(instance);
            result.setAttribute("request", fixture.getRequest());
            result.setAttribute("response", fixture.getResponse());
        }
    }

    /**
     * Gets diagnostic information about a request message. If the request
     * contains a message body, it should be represented as a DOM Document node
     * or as an object having a meaningful toString() implementation.
     *
     * @param req An object representing an HTTP request message.
     * @return A string containing information gleaned from the request message.
     */
    String getRequestMessageInfo(ClientRequest req) {
        if (null == req) {
            return "No request message.";
        }
        StringBuilder msgInfo = new StringBuilder();
        msgInfo.append("Method: ").append(req.getMethod()).append('\n');
        msgInfo.append("Target URI: ").append(req.getURI()).append('\n');
        msgInfo.append("Headers: ").append(req.getHeaders()).append('\n');
        if (null != req.getEntity()) {
            Object entity = req.getEntity();
            String body;
            if (Document.class.isInstance(entity)) {
                Document doc = Document.class.cast(entity);
                body = XMLUtils.writeNodeToString(doc);
            } else {
                body = entity.toString();
            }
            msgInfo.append(body).append('\n');
        }
        return msgInfo.toString();
    }

    /**
     * Gets diagnostic information about a response message.
     *
     * @param rsp An object representing an HTTP response message.
     * @return A string containing information gleaned from the response
     * message.
     */
    String getResponseMessageInfo(ClientResponse rsp) {
        if (null == rsp) {
            return "No response message.";
        }
        StringBuilder msgInfo = new StringBuilder();
        msgInfo.append("Status: ").append(rsp.getStatus()).append('\n');
        msgInfo.append("Headers: ").append(rsp.getHeaders()).append('\n');
        if (rsp.hasEntity()) {
            if (rsp.getType().isCompatible(MediaType.APPLICATION_XML_TYPE)) {
                Document doc = ClientUtils.getResponseEntityAsDocument(rsp, null);
                msgInfo.append(XMLUtils.writeNodeToString(doc));
            } else {
                byte[] body = rsp.getEntity(byte[].class);
                msgInfo.append(new String(body, StandardCharsets.UTF_8));
            }
            msgInfo.append('\n');
        }
        return msgInfo.toString();
    }

}
