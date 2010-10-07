package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Templatable;

/**
 *
 *
 * @author brad
 */
public class SecureReadAuthoriser implements ClydeAuthoriser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SecureReadAuthoriser.class );
    private final ClydeAuthoriser wrapped;

    public SecureReadAuthoriser( ClydeAuthoriser wrapped ) {
        this.wrapped = wrapped;
    }

    @Override
    public String getName() {
        return SecureReadAuthoriser.class.getCanonicalName() + "( " + wrapped.getName() + " )";
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method, Auth auth ) {
        if( resource instanceof Templatable ) {
            return authoriseClydeResource( (Templatable) resource, request, method, auth );
        } else {
            // dont know how to handle this resource
            return null;
        }
    }

    private Boolean authoriseClydeResource( Templatable templatable, Request request, Method method, Auth auth ) {
        if( templatable instanceof Folder ) {
            log.debug( "check folder: method: " + method);
            Folder folder = (Folder) templatable;
            boolean isWrite = isWriteMethod(method);
            if( folder.isSecureRead() || isWrite ) {
                if( log.isTraceEnabled()) {
                    log.trace( "is secure or write method, so if not logged in definitely not: isWrite:" + isWrite );
                }
                if( request.getAuthorization() == null ) {
                    if(log.isTraceEnabled()) {
                        log.trace( "not logged in. deny access. secureread:" + folder.isSecureRead() + " isWrite:" + isWrite + " folder:" + folder.getHref() );
                    }
                    return false;
                } else {
                    if( log.isTraceEnabled()) {
                        log.trace( "delegating authorisation to: " + wrapped.getClass().getCanonicalName() );
                    }
                    boolean result = wrapped.authorise( folder, request, method, auth );
                    if( !result && log.isDebugEnabled() ) {
                        log.debug( "wrapped authoriser said deny access: " + wrapped.getClass() + " folder:" + folder.getHref());
                    }
                    return result;
                }
            } else {
                log.debug( "not secureRead and not a write method, so dont care" );
            }
            // is secure, and logged in so might be ok. up to someone else to decide
            return null;
        } else {
            Folder folder = templatable.getParentFolder();
            if( log.isDebugEnabled()) {
                log.debug( "Can't check type:" + templatable.getClass().getCanonicalName() + ", try:" + folder.getHref());
            }
            return authoriseClydeResource( folder, request, method, auth );
        }
    }

    private boolean isWriteMethod( Method method ) {
        if( method == Method.POST) return false;  // we want POST authorisation to be done by components
        return method.isWrite;
    }
}
