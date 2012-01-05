package com.ettrema.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.BaseResource;
import com.ettrema.web.RequestParams;
import java.util.ArrayList;
import java.util.List;

/**
 * This will permit access for a particular contentType and methods.
 *
 * For example, it might allow GET and HEAD on image types to allow
 * photo printing partners access to our photos
 *
 * Access is only granted if there is a request parameter containing the magic number
 *
 * This authoriser will generally not decline access, it will only allow or express no opinion.
 *
 * The only situation in which it will decline access, is if the content type
 * and method matches, and the request param is given, but the value is incorrect
 *
 * @author brad
 */
public class MagicNumberAuthoriser implements ClydeAuthoriser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MagicNumberAuthoriser.class );
    private final List<Request.Method> methods;
    private final MagicNumberStrategy strategy;
    private final String contentType;
    private final String magicNumberParameterName;

    /**
     *
     * @param methods - comma seperated list of methods
     * @param contentType
     * @param magicNumberParameterName
     */
    public MagicNumberAuthoriser( String methods, String contentType, String magicNumberParameterName ) {
        this.methods = new ArrayList<Method>();
        for( String m : methods.split( ",") ) {
            this.methods.add( Method.valueOf( m ) );
        }

        this.strategy = new ClydeMagicNumberStrategy();
        this.contentType = contentType;
        this.magicNumberParameterName = magicNumberParameterName;
    }

    /**
     * 
     * @param methods - the methods which may be permitted
     * @param strategy - used to calculate the magic number for a resource
     * @param contentType - the contentType of resources to check. May be null, which means it wont be used
     * @param magicNumberParameterName - the name of the request parameter containing the magic number
     */
    public MagicNumberAuthoriser( List<String> methods, MagicNumberStrategy strategy, String contentType, String magicNumberParameterName ) {
        this.methods = new ArrayList<Method>();
        for( String m : methods ) {
            this.methods.add( Method.valueOf( m ) );
        }
        this.strategy = strategy;
        this.contentType = contentType;
        this.magicNumberParameterName = magicNumberParameterName;
    }

    @Override
    public String getName() {
        return this.getClass().getName() + " " + methods.toString();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method, Auth auth ) {
        log.trace( "authorise" );
        if( contentType != null ) {
            GetableResource gr = (GetableResource) resource;
            String actualContentType = gr.getContentType( null );
            if( actualContentType != null && actualContentType.contains( contentType ) ) {
                log.trace( "matching contenttype" );
                return checkAccess( resource, request, method );
            } else {
                log.trace( "not matching contentType" );
                return null; // no opinion
            }
        } else {
            return checkAccess( resource, request, method );
        }
    }

    private Boolean checkAccess( Resource resource, Request request, Method method ) {
        log.trace( "checkAccess" );
        if(RequestParams.current() == null ) {
            return null;
        }
        String reqNum = request.getParams().get( magicNumberParameterName );
        if( reqNum == null || reqNum.length() == 0 ) {
            log.trace( "no request param" );
            return null;
        }
        Method m = method;
        if( !methods.contains( m ) ) {
            log.trace( "not matching method" );
            return null;
        }
        String actualNum = strategy.getMagicNumber( resource );
        log.debug( "actual: " + actualNum);
        log.debug( "req: " + reqNum);
        if( reqNum.equals( actualNum ) ) {
            log.trace( "allow access" );
            return Boolean.TRUE;
        } else {
            log.trace( "incorrect magic number" );
            return Boolean.FALSE;
        }
    }

    public interface MagicNumberStrategy {

        String getMagicNumber( Resource r );
    }

    public static class ClydeMagicNumberStrategy implements MagicNumberStrategy {

        @Override
        public String getMagicNumber( Resource r ) {
            if( r instanceof BaseResource ) {
                BaseResource res = (BaseResource) r;
                return res.getMagicNumber();
            } else {
                log.warn( "get magic number for a non-persisted resource" );
                return r.hashCode() + "";
            }
        }
    }
}
