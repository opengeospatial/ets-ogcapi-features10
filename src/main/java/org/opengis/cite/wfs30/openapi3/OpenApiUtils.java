package org.opengis.cite.wfs30.openapi3;

import static org.opengis.cite.wfs30.openapi3.OpenApiUtils.PATH.API;
import static org.opengis.cite.wfs30.openapi3.OpenApiUtils.PATH.COLLECTIONS;
import static org.opengis.cite.wfs30.openapi3.OpenApiUtils.PATH.CONFORMANCE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.reprezen.kaizen.oasparser.model3.MediaType;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.model3.Operation;
import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.Path;
import com.reprezen.kaizen.oasparser.model3.Response;
import com.reprezen.kaizen.oasparser.model3.Schema;
import com.reprezen.kaizen.oasparser.model3.Server;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.api.uri.UriTemplateParser;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class OpenApiUtils {

    // as described in https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#fixed-fields
    private static final String DEFAULT_SERVER_URL = "/";

    @FunctionalInterface
    private interface PathMatcherFunction<A, B, C> {
        A apply( B b, C c );
    }

    enum PATH {

        API( "api" ), CONFORMANCE( "conformance" ), COLLECTIONS( "collections" );

        private String pathItem;

        PATH( String pathItem ) {

            this.pathItem = pathItem;
        }

        private String getPathItem() {
            return pathItem;
        }
    }

    private static class PathMatcher implements PathMatcherFunction<Boolean, String, String> {
        @Override
        public Boolean apply( String pathUnderTest, String pathToMatch ) {
            UriTemplateParser parser = new UriTemplateParser( pathUnderTest );
            Matcher matcher = parser.getPattern().matcher( pathToMatch );
            return matcher.matches();
        }
    }

    private static class ExactMatchFilter implements Predicate<TestPoint> {

        private final String requestedPath;

        ExactMatchFilter( String requestedPath ) {
            this.requestedPath = requestedPath;
        }

        @Override
        public boolean test( TestPoint testPoint ) {
            UriTemplate uriTemplate = new UriTemplate( testPoint.getPath() );
            Map<String, String> templateReplacement = new HashMap<>( testPoint.getPredefinedTemplateReplacement() );
            List<String> templateVariables = uriTemplate.getTemplateVariables();
            for ( String templateVariable : templateVariables ) {
                if ( !templateReplacement.containsKey( templateVariable ) )
                    templateReplacement.put( templateVariable, ".*" );
            }
            String uri = uriTemplate.createURI( templateReplacement );
            Pattern pattern = Pattern.compile( uri );
            return pattern.matcher( requestedPath ).matches();
        }
    }

    private OpenApiUtils() {
    }

    /**
     * Parse all test points from the passed OpenApi3 document as described in A.4.3. Identify the Test Points.
     *
     * @param apiModel
     *            never <code>null</code>
     * @return the parsed test points, may be empty but never <code>null</code>
     */
    static List<TestPoint> retrieveTestPoints( OpenApi3 apiModel ) {
        List<Path> pathItemObjects = identifyTestPoints( apiModel );
        List<PathItemAndServer> pathItemAndServers = identifyServerUrls( apiModel, pathItemObjects );
        return processServerObjects( pathItemAndServers );
    }

    /**
     * Parse the API test points from the passed OpenApi3 document as described in A.4.3. Identify the Test Points.
     *
     * @param apiModel
     *            never <code>null</code>
     * @return the parsed test points, may be empty but never <code>null</code>
     */
    static List<TestPoint> retrieveTestPointsForApi( OpenApi3 apiModel ) {
        return retrieveTestPoints( apiModel, API );
    }

    /**
     * Parse the CONFORMANCE test points from the passed OpenApi3 document as described in A.4.3. Identify the Test
     * Points.
     *
     * @param apiModel
     *            never <code>null</code>
     * @return the parsed test points, may be empty but never <code>null</code>
     */
    public static List<TestPoint> retrieveTestPointsForConformance( OpenApi3 apiModel ) {
        return retrieveTestPoints( apiModel, CONFORMANCE );
    }

    /**
     * Parse the COLLECTIONS METADATA test points from the passed OpenApi3 document as described in A.4.3. Identify the
     * Test Points.
     *
     * @param apiModel
     *            never <code>null</code>
     * @return the parsed test points, may be empty but never <code>null</code>
     */
    public static List<TestPoint> retrieveTestPointsForCollectionsMetadata( OpenApi3 apiModel ) {
        return retrieveTestPoints( apiModel, COLLECTIONS );
    }

    /**
     * Parse the COLLECTION METADATA test points for the passed collectionName including the extended path from the
     * passed OpenApi3 document as described in A.4.3. Identify the Test Points.
     *
     * @param apiModel
     *            never <code>null</code>
     * @param collectionName
     *            the extended path, may be <code>null</code>
     * @return the parsed test points, may be empty but never <code>null</code>
     */
    public static List<TestPoint> retrieveTestPointsForCollectionMetadata( OpenApi3 apiModel, String collectionName ) {
        StringBuilder requestedPath = new StringBuilder();
        requestedPath.append( "/" );
        requestedPath.append( COLLECTIONS.getPathItem() );
        requestedPath.append( "/" );
        requestedPath.append( collectionName );

        List<TestPoint> testPoints = retrieveTestPoints( apiModel, requestedPath.toString() );
        return testPoints.stream().filter( new ExactMatchFilter( requestedPath.toString() ) ).collect( Collectors.toList() );
    }

    /**
     * Parse the COLLECTIONS test points from the passed OpenApi3 document as described in A.4.3. Identify the Test
     * Points.
     *
     * @param apiModel
     *            never <code>null</code>
     * @param noOfCollection
     *            the number of collections to return test points for (-1 means the test points of all collections
     *            should be returned)
     * @return the parsed test points, may be empty but never <code>null</code>
     */
    public static List<TestPoint> retrieveTestPointsForCollections( OpenApi3 apiModel, int noOfCollection ) {
        StringBuilder requestedPath = new StringBuilder();
        requestedPath.append( "/" );
        requestedPath.append( COLLECTIONS.getPathItem() );
        requestedPath.append( "/.*/items" );

        List<TestPoint> allTestPoints = retrieveTestPoints( apiModel, requestedPath.toString(),
                                                            ( a, b ) -> a.matches( b ) );
        if ( noOfCollection < 0 || allTestPoints.size() <= noOfCollection ) {
            return allTestPoints;
        }
        return allTestPoints.subList( 0, noOfCollection );
    }

    /**
     * Parse the test points with the passed path including the extended path from the passed OpenApi3 document as
     * described in A.4.3. Identify the Test Points.
     *
     * @param apiModel
     *            never <code>null</code>
     * @param collectionName
     *            the extended path, may be <code>null</code>
     * @return the parsed test points, may be empty but never <code>null</code>
     */
    public static List<TestPoint> retrieveTestPointsForCollection( OpenApi3 apiModel, String collectionName ) {
        StringBuilder requestedPath = new StringBuilder();
        requestedPath.append( "/" );
        requestedPath.append( COLLECTIONS.getPathItem() );
        requestedPath.append( "/" );
        requestedPath.append( collectionName );
        requestedPath.append( "/items" );

        List<TestPoint> testPoints = retrieveTestPoints( apiModel, requestedPath.toString() );
        return testPoints.stream().filter( new ExactMatchFilter( requestedPath.toString() ) ).collect( Collectors.toList() );
    }

    /**
     * Parse the test points with the passed path including the extended path from the passed OpenApi3 document as
     * described in A.4.3. Identify the Test Points.
     *
     * @param apiModel
     *            never <code>null</code>
     * @param collectionName
     *            the extended path, may be <code>null</code>
     * @param featureId
     *            the id of the feature, never <code>null</code>
     * @return the parsed test points, may be empty but never <code>null</code>
     */
    public static List<TestPoint> retrieveTestPointsForFeature( OpenApi3 apiModel, String collectionName,
                                                                String featureId ) {
        StringBuilder requestedPath = new StringBuilder();
        requestedPath.append( "/" );
        requestedPath.append( COLLECTIONS.getPathItem() );
        requestedPath.append( "/" );
        requestedPath.append( collectionName );
        requestedPath.append( "/items/" );
        requestedPath.append( featureId );

        List<TestPoint> testPoints = retrieveTestPoints( apiModel, requestedPath.toString() );
        return testPoints.stream().filter( new ExactMatchFilter( requestedPath.toString() ) ).collect( Collectors.toList() );
    }

    private static List<TestPoint> retrieveTestPoints( OpenApi3 apiModel, PATH path ) {
        String requestedPath = "/" + path.getPathItem();
        return retrieveTestPoints( apiModel, requestedPath );
    }

    private static List<TestPoint> retrieveTestPoints( OpenApi3 apiModel, String requestedPath ) {
        return retrieveTestPoints( apiModel, requestedPath, new PathMatcher() );
    }

    private static List<TestPoint> retrieveTestPoints( OpenApi3 apiModel, String requestedPath,
                                                       PathMatcherFunction<Boolean, String, String> pathMatcher ) {
        List<Path> pathItemObjects = identifyTestPoints( apiModel, requestedPath, pathMatcher );
        List<PathItemAndServer> pathItemAndServers = identifyServerUrls( apiModel, pathItemObjects );
        return processServerObjects( pathItemAndServers );
    }

    /**
     * A.4.3.1. Identify Test Points:
     *
     * a) Purpose: To identify the test points associated with each Path in the OpenAPI document
     *
     * b) Pre-conditions:
     *
     * An OpenAPI document has been obtained
     *
     * A list of URLs for the servers to be included in the compliance test has been provided
     *
     * A list of the paths specified in the WFS 3.0 specification
     *
     * c) Method:
     *
     * FOR EACH paths property in the OpenAPI document If the path name is one of those specified in the WFS 3.0
     * specification Retrieve the Server URIs using A.4.3.2. FOR EACH Server URI Concatenate the Server URI with the
     * path name to form a test point. Add that test point to the list.
     *
     * d) References: None
     *
     * @param apiModel
     *            never <code>null</code>
     */
    private static List<Path> identifyTestPoints( OpenApi3 apiModel ) {
        List<Path> allTestPoints = new ArrayList<>();
        for ( PATH path : PATH.values() )
            allTestPoints.addAll( identifyTestPoints( apiModel, "/" + path.getPathItem(), new PathMatcher() ) );
        return allTestPoints;
    }

    private static List<Path> identifyTestPoints( OpenApi3 apiModel, String path,
                                                  PathMatcherFunction<Boolean, String, String> pathMatch ) {
        List<Path> pathItems = new ArrayList<>();
        Map<String, Path> pathItemObjects = apiModel.getPaths();
        for ( Path pathItemObject : pathItemObjects.values() ) {
            String pathString = pathItemObject.getPathString();
            if ( pathMatch.apply( pathString, path ) ) {
                pathItems.add( pathItemObject );
            }
        }
        return pathItems;
    }

    /**
     * A.4.3.2. Identify Server URIs:
     *
     * a) Purpose: To identify all server URIs applicable to an OpenAPI Operation Object
     *
     * b) Pre-conditions:
     *
     * Server Objects from the root level of the OpenAPI document have been obtained
     *
     * A Path Item Object has been retrieved
     *
     * An Operation Object has been retrieved
     *
     * The Operation Object is associated with the Path Item Object
     *
     * A list of URLs for the servers to be included in the compliance test has been provided
     *
     * c) Method:
     *
     * 1) Identify the Server Objects which are in-scope for this operationObject
     *
     * IF Server Objects are defined at the Operation level, then those and only those Server Objects apply to that
     * Operation.
     *
     * IF Server Objects are defined at the Path Item level, then those and only those Server Objects apply to that Path
     * Item.
     *
     * IF Server Objects are not defined at the Operation level, then the Server Objects defined for the parent Path
     * Item apply to that Operation.
     *
     * IF Server Objects are not defined at the Path Item level, then the Server Objects defined for the root level
     * apply to that Path.
     *
     * IF no Server Objects are defined at the root level, then the default server object is assumed as described in the
     * OpenAPI specification.
     *
     * 2) Process each Server Object using A.4.3.3.
     *
     * 3) Delete any Server URI which does not reference a server on the list of servers to test.
     *
     * d) References: None
     * 
     * @param apiModel
     *            never <code>null</code>
     * @param pathItemObjects
     *            never <code>null</code>
     */
    private static List<PathItemAndServer> identifyServerUrls( OpenApi3 apiModel, List<Path> pathItemObjects ) {
        List<PathItemAndServer> pathItemAndServers = new ArrayList<>();

        for ( Path pathItemObject : pathItemObjects ) {
            Map<String, Operation> operationObjects = pathItemObject.getOperations();
            for ( Operation operationObject : operationObjects.values() ) {
                List<String> serverUrls = identifyServerObjects( apiModel, pathItemObject, operationObject );
                for ( String serverUrl : serverUrls ) {
                    PathItemAndServer pathItemAndServer = new PathItemAndServer( pathItemObject, operationObject,
                                                                                 serverUrl );
                    pathItemAndServers.add( pathItemAndServer );
                }
            }
        }
        return pathItemAndServers;
    }

    /**
     * A.4.3.3. Process Server Object:
     *
     * a) Purpose: To expand the contents of a Server Object into a set of absolute URIs.
     *
     * b) Pre-conditions: A Server Object has been retrieved
     *
     * c) Method:
     *
     * Processing the Server Object results in a set of absolute URIs. This set contains all of the URIs that can be
     * created given the URI template and variables defined in that Server Object.
     *
     * If there are no variables in the URI template, then add the URI to the return set.
     *
     * For each variable in the URI template which does not have an enumerated set of valid values:
     *
     * generate a URI using the default value,
     *
     * add this URI to the return set,
     *
     * flag this URI as non-exhaustive
     *
     * For each variable in the URI template which has an enumerated set of valid values:
     *
     * generate a URI for each value in the enumerated set,
     *
     * add each generated URI to the return set.
     *
     * Perform this processing in an iterative manner so that there is a unique URI for all possible combinations of
     * enumerated and default values.
     *
     * Convert all relative URIs to absolute URIs by rooting them on the URI to the server hosting the OpenAPI document.
     *
     * d) References: None
     *
     * @param pathItemAndServers
     *            never <code>null</code>
     */
    private static List<TestPoint> processServerObjects( List<PathItemAndServer> pathItemAndServers ) {
        List<TestPoint> uris = new ArrayList<>();
        for ( PathItemAndServer pathItemAndServer : pathItemAndServers ) {
            processServerObject( uris, pathItemAndServer );
        }
        return uris;
    }

    private static void processServerObject( List<TestPoint> uris, PathItemAndServer pathItemAndServer ) {
        String pathString = pathItemAndServer.pathItemObject.getPathString();
        Response response = getResponse(pathItemAndServer);
        if ( response == null )
            return;
        Map<String, MediaType> contentMediaTypes = response.getContentMediaTypes();

        UriTemplate uriTemplate = new UriTemplate( pathItemAndServer.serverUrl + pathString );
        if ( uriTemplate.getNumberOfTemplateVariables() == 0 ) {
            TestPoint testPoint = new TestPoint( pathItemAndServer.serverUrl, pathString, contentMediaTypes );
            uris.add( testPoint );
        } else {
            List<Map<String, String>> templateReplacements = collectTemplateReplacements( pathItemAndServer,
                                                                                          uriTemplate );

            if ( templateReplacements.isEmpty() ) {
                TestPoint testPoint = new TestPoint( pathItemAndServer.serverUrl, pathString, contentMediaTypes );
                uris.add( testPoint );
            } else {
                for ( Map<String, String> templateReplacement : templateReplacements ) {
                    TestPoint testPoint = new TestPoint( pathItemAndServer.serverUrl, pathString, templateReplacement,
                                                         contentMediaTypes );
                    uris.add( testPoint );
                }
            }
        }
    }

    private static Response getResponse( PathItemAndServer pathItemAndServer ) {
        if ( pathItemAndServer.operationObject.hasResponse( "200" ) )
            return pathItemAndServer.operationObject.getResponse( "200" );
        if ( pathItemAndServer.operationObject.hasResponse( "default" ) )
            return pathItemAndServer.operationObject.getResponse( "default" );
        return null;
    }

    private static List<Map<String, String>> collectTemplateReplacements( PathItemAndServer pathItemAndServer,
                                                                          UriTemplate uriTemplate ) {
        List<Map<String, String>> templateReplacements = new ArrayList<>();
        Collection<Parameter> parameters = pathItemAndServer.operationObject.getParameters();
        for ( String templateVariable : uriTemplate.getTemplateVariables() ) {
            for ( Parameter parameter : parameters ) {
                if ( templateVariable.equals( parameter.getName() ) ) {
                    Schema schema = parameter.getSchema();
                    if ( schema.hasEnums() ) {
                        addEnumTemplateValues( templateReplacements, templateVariable, schema );
                    } else if ( schema.getDefault() != null ) {
                        addDefaultTemplateValue( templateReplacements, templateVariable, schema );
                    } else {
                        // TODO: What should be done if the parameter does not have a default value and no
                        // enumerated set of valid values?
                    }
                }
            }
        }
        return templateReplacements;
    }

    private static void addEnumTemplateValues( List<Map<String, String>> templateReplacements, String templateVariable,
                                               Schema schema ) {
        Collection<Object> enums = schema.getEnums();
        if ( enums.size() == 1 ) {
            for ( Object enumValue : enums ) {
                Map<String, String> replacement = new HashMap<>();
                replacement.put( templateVariable, enumValue.toString() );
                templateReplacements.add( replacement );
            }
        } else {
            if ( templateReplacements.isEmpty() ) {
                Map<String, String> replacement = new HashMap<>();
                templateReplacements.add( replacement );
            }
            List<Map<String, String>> templateReplacementsToAdd = new ArrayList<>();
            for ( Map<String, String> templateReplacement : templateReplacements ) {
                for ( Object enumValue : enums ) {
                    Map<String, String> newTemplateReplacement = new HashMap<>();
                    newTemplateReplacement.putAll( templateReplacement );
                    newTemplateReplacement.put( templateVariable, enumValue.toString() );
                    templateReplacementsToAdd.add( newTemplateReplacement );
                }
            }
            templateReplacements.clear();
            templateReplacements.addAll( templateReplacementsToAdd );
        }
    }

    private static void addDefaultTemplateValue( List<Map<String, String>> templateReplacements,
                                                 String templateVariable, Schema schema ) {
        if ( templateReplacements.isEmpty() ) {
            Map<String, String> replacement = new HashMap<>();
            templateReplacements.add( replacement );
        }
        for ( Map<String, String> templateReplacement : templateReplacements ) {
            templateReplacement.put( templateVariable, schema.getDefault().toString() );
        }
    }

    private static List<String> identifyServerObjects( OpenApi3 apiModel, Path pathItemObject, Operation operationObject ) {
        if ( operationObject.hasServers() )
            return parseUrls( operationObject.getServers() );
        if ( pathItemObject.hasServers() )
            return parseUrls( pathItemObject.getServers() );
        if ( apiModel.hasServers() )
            return parseUrls( apiModel.getServers() );
        return Collections.singletonList( DEFAULT_SERVER_URL );
    }

    private static List<String> parseUrls( Collection<Server> servers ) {
        List<String> urls = new ArrayList<>();
        for ( Server server : servers )
            urls.add( server.getUrl() );
        return urls;
    }

    private static class PathItemAndServer {

        private final Path pathItemObject;

        private Operation operationObject;

        // TODO: must be a server object to consider server variables
        private String serverUrl;

        private PathItemAndServer( Path pathItemObject, Operation operationObject, String serverUrl ) {
            this.pathItemObject = pathItemObject;
            this.operationObject = operationObject;
            this.serverUrl = serverUrl;
        }

    }

}
