package org.opengis.cite.wfs30;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Buffers the (response) entity so it can be read multiple times.
 *
 * <p><strong>WARNING:</strong> The entity InputStream must be reset after each
 * read attempt.</p>
 */
public class ReusableEntityFilter extends ClientFilter {

    @Override
    public ClientResponse handle(ClientRequest req) throws ClientHandlerException {
        // leave request entity--it can usually be read multiple times
        ClientResponse rsp = getNext().handle(req);
        if (rsp.hasEntity()) {
            rsp.bufferEntity();
        }
        return rsp;
    }

}
