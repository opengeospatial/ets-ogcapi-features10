package org.opengis.cite.wfs30.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.logging.Level;

import javax.ws.rs.core.HttpHeaders;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Provides a collection of utility methods for manipulating or resolving URI references.
 */
public class URIUtils {

    /**
     * Dereferences the given URI and stores the resulting resource representation in a local file. The file will be
     * located in the default temporary file directory.
     * 
     * @param uriRef
     *            An absolute URI specifying the location of some resource.
     * @return A File containing the content of the resource; it may be empty if resolution failed for any reason.
     * @throws IOException
     *             If an IO error occurred.
     */
    public static File dereferenceURI( URI uriRef )
                            throws IOException {
        if ( ( null == uriRef ) || !uriRef.isAbsolute() ) {
            throw new IllegalArgumentException( "Absolute URI is required, but received " + uriRef );
        }
        if ( uriRef.getScheme().equalsIgnoreCase( "file" ) ) {
            return new File( uriRef );
        }
        Client client = Client.create();
        WebResource webRes = client.resource( uriRef );
        ClientResponse rsp = webRes.get( ClientResponse.class );
        String suffix = null;
        if ( rsp.getHeaders().getFirst( HttpHeaders.CONTENT_TYPE ).endsWith( "xml" ) ) {
            suffix = ".xml";
        }
        File destFile = File.createTempFile( "entity-", suffix );
        if ( rsp.hasEntity() ) {
            InputStream is = rsp.getEntityInputStream();
            OutputStream os = new FileOutputStream( destFile );
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ( ( bytesRead = is.read( buffer ) ) != -1 ) {
                os.write( buffer, 0, bytesRead );
            }
            is.close();
            os.flush();
            os.close();
        }
        TestSuiteLogger.log( Level.FINE,
                             "Wrote " + destFile.length() + " bytes to file at " + destFile.getAbsolutePath() );
        return destFile;
    }

}
