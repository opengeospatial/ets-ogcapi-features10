package org.opengis.cite.wfs30.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.opengis.cite.wfs30.ReusableEntityFilter;
import org.w3c.dom.Document;

/**
 * Provides various utility methods for creating and configuring HTTP client
 * components.
 */
public class ClientUtils {

    /**
     * Builds a client component for interacting with HTTP endpoints. The client
     * will automatically redirect to the URI declared in 3xx responses. The
     * connection timeout is 10 s. Request and response messages may be logged
     * to a JDK logger (in the namespace "com.sun.jersey.api.client").
     *
     * @return A Client component.
     */
    public static Client buildClient() {
        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(
                ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        config.getProperties().put(
                ClientConfig.PROPERTY_CONNECT_TIMEOUT, 10000);
        Client client = Client.create(config);
        client.addFilter(new ReusableEntityFilter());
        client.addFilter(new LoggingFilter());
        return client;
    }

    /**
     * Constructs a client component that uses a specified web proxy. Proxy
     * authentication is not supported. Configuring the client to use an
     * intercepting proxy can be useful when debugging a test.
     *
     * @param proxyHost The host name or IP address of the proxy server.
     * @param proxyPort The port number of the proxy listener.
     *
     * @return A Client component that submits requests through a web proxy.
     */
    public static Client buildClientWithProxy(final String proxyHost,
            final int proxyPort) {
        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(
                ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        Client client = new Client(new URLConnectionClientHandler(
                new HttpURLConnectionFactory() {
                    SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

                    @Override
                    public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
                        return (HttpURLConnection) url.openConnection(proxy);
                    }
                }), config);
        client.addFilter(new LoggingFilter());
        return client;
    }

    /**
     * Builds an HTTP request message that uses the GET method.
     *
     * @param endpoint A URI indicating the target resource.
     * @param qryParams A Map containing query parameters (may be null);
     * @param mediaTypes A list of acceptable media types; if not specified,
     * generic XML ("application/xml") is preferred.
     *
     * @return A ClientRequest object.
     */
    public static ClientRequest buildGetRequest(URI endpoint,
            Map<String, String> qryParams, MediaType... mediaTypes) {
        UriBuilder uriBuilder = UriBuilder.fromUri(endpoint);
        if (null != qryParams) {
            for (Map.Entry<String, String> param : qryParams.entrySet()) {
                uriBuilder.queryParam(param.getKey(), param.getValue());
            }
        }
        URI uri = uriBuilder.build();
        ClientRequest.Builder reqBuilder = ClientRequest.create();
        if (null == mediaTypes || mediaTypes.length == 0) {
            reqBuilder = reqBuilder.accept(MediaType.APPLICATION_XML_TYPE);
        } else {
            reqBuilder = reqBuilder.accept(mediaTypes);
        }
        ClientRequest req = reqBuilder.build(uri, HttpMethod.GET);
        return req;
    }

    /**
     * Creates a copy of the given MediaType object but without any parameters.
     *
     * @param mediaType A MediaType descriptor.
     * @return A new (immutable) MediaType object having the same type and
     * subtype.
     */
    public static MediaType removeParameters(MediaType mediaType) {
        return new MediaType(mediaType.getType(), mediaType.getSubtype());
    }

    /**
     * Obtains the (XML) response entity as a JAXP Source object and resets the
     * entity input stream for subsequent reads.
     *
     * @param response A representation of an HTTP response message.
     * @param targetURI The target URI from which the entity was retrieved (may
     * be null).
     * @return A Source to read the entity from; its system identifier is set
     * using the given targetURI value (this may be used to resolve any relative
     * URIs found in the source).
     */
    public static Source getResponseEntityAsSource(ClientResponse response,
            String targetURI) {
        Source source = response.getEntity(DOMSource.class);
        if (null != targetURI && !targetURI.isEmpty()) {
            source.setSystemId(targetURI);
        }
        if (response.getEntityInputStream().markSupported()) {
            try {
                // NOTE: entity was buffered by client filter
                response.getEntityInputStream().reset();
            } catch (IOException ex) {
                Logger.getLogger(ClientUtils.class.getName()).log(Level.WARNING,
                        "Failed to reset response entity.", ex);
            }
        }
        return source;
    }

    /**
     * Obtains the (XML) response entity as a DOM Document and resets the entity
     * input stream for subsequent reads.
     *
     * @param response A representation of an HTTP response message.
     * @param targetURI The target URI from which the entity was retrieved (may
     * be null).
     * @return A Document representing the entity; its base URI is set using the
     * given targetURI value (this may be used to resolve any relative URIs
     * found in the document).
     */
    public static Document getResponseEntityAsDocument(ClientResponse response,
            String targetURI) {
        DOMSource domSource = (DOMSource) getResponseEntityAsSource(response,
                targetURI);
        Document entityDoc = (Document) domSource.getNode();
        entityDoc.setDocumentURI(domSource.getSystemId());
        return entityDoc;
    }
}
