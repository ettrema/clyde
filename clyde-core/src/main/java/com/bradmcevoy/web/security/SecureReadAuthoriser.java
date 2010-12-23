package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.Templatable;

/**
 * This class checks to see if any parent folder of the requested resource
 * has the secureRead flag set. If it does then requests are authorised by
 * the wrapped authoriser.
 *
 * This is effectively a means to disable permission checking by having the secure
 * flag unset.
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
            if( !isSecure( (Templatable) resource ) && isReadMethod( method ) ) {
                log.trace("authorise: templatable, not secure and is read method so allow");
                return Boolean.TRUE;
            } else {
                log.trace("authorise: templatable, is secure or write method so delegate authorisation");
                return wrapped.authorise( resource, request, method, auth );
            }
            //return authoriseClydeResource( (Templatable) resource, request, method, auth );
        } else {
            // dont know how to handle this resource
            log.trace("authorise: not a known resource type so no opinion");
            return null;
        }
    }

//    private Boolean authoriseClydeResource( Templatable templatable, Request request, Method method, Auth auth ) {
//        if( templatable instanceof Folder ) {
//            log.debug( "check folder: method: " + method );
//            Folder folder = (Folder) templatable;
//            boolean isWrite = isWriteMethod( method );
//            if( folder.isSecureRead() || isWrite ) {
//                if( log.isTraceEnabled() ) {
//                    log.trace( "is secure or write method, so if not logged in definitely not: isWrite:" + isWrite );
//                }
//                if( request.getAuthorization() == null ) {
//                    if( log.isTraceEnabled() ) {
//                        log.trace( "not logged in. deny access. secureread:" + folder.isSecureRead() + " isWrite:" + isWrite + " folder:" + folder.getHref() );
//                    }
//                    return false;
//                } else {
//                    if( log.isTraceEnabled() ) {
//                        log.trace( "delegating authorisation to: " + wrapped.getClass().getCanonicalName() );
//                    }
//                    boolean result = wrapped.authorise( folder, request, method, auth );
//                    if( !result && log.isDebugEnabled() ) {
//                        log.debug( "wrapped authoriser said deny access: " + wrapped.getClass() + " folder:" + folder.getHref() );
//                    }
//                    return result;
//                }
//            } else {
//                log.debug( "not secureRead and not a write method, so dont care" );
//            }
//            // is secure, and logged in so might be ok. up to someone else to decide
//            return null;
//        } else {
//            Folder folder = templatable.getParentFolder();
//            if( log.isDebugEnabled() ) {
//                log.debug( "Can't check type:" + templatable.getClass().getCanonicalName() + ", try:" + folder.getHref() );
//            }
//            return authoriseClydeResource( folder, request, method, auth );
//        }
//    }

    private boolean isWriteMethod( Method method ) {
        if( method == Method.POST ) return false;  // we want POST authorisation to be done by components
        return method.isWrite;
    }

    private boolean isReadMethod( Method method ) {
        return !isWriteMethod( method );
    }

    private boolean isSecure( Templatable templatable ) {
        if( templatable instanceof Host ) {
            Host h = (Host) templatable;
            return h.isSecureRead();
        } else if( templatable instanceof Folder ) {
            Folder folder = (Folder) templatable;
            if( folder.isSecureRead() ) {
                return true;
            } else {
                return false;
            }
        } else {
            Folder folder = templatable.getParentFolder();
            return isSecure( folder );
        }
    }
}
