package com.bradmcevoy.web.security;

import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author brad
 */
public class CookieAuthenticationHandler implements AuthenticationHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CookieAuthenticationHandler.class );
    private String requestParamName = "_clydeauthid";
    private String cookieName = "_clydeauthid";
    private final List<AuthenticationHandler> handlers;
    private final Map<String, UUID> authIdMap = new ConcurrentHashMap<String, UUID>();

    public CookieAuthenticationHandler( List<AuthenticationHandler> handlers ) {
        this.handlers = handlers;
    }

    public boolean supports( Resource r, Request request ) {
        // We will support it if there is either a auth id request param, or a cookie
        String authId = getAuthId( request );
        if( authId != null ) {
            log.debug( "found authId: " + authId );
            return true;
        } else {
            for( AuthenticationHandler hnd : handlers ) {
                if( hnd.supports( r, request ) ) {
                    request.getAttributes().put( "_delegatedAuthenticationHandler", hnd );
                    return true;
                }
            }
            return false;
        }

    }

    public Object authenticate( Resource resource, Request request ) {
        String authId = getAuthId( request );
        if( authId != null ) {
            UUID id = authIdMap.get( authId );
            if( id == null ) {
                log.warn( "Found authId, but no corresponding UUID" );
                return null;
            } else {
                VfsSession session = RequestContext.getCurrent().get( VfsSession.class );
                if( session == null )
                    throw new IllegalStateException( "no context" );
                NameNode node = session.get( id );
                if( node == null ) {
                    log.warn( "Couldnt find node for id: " + id );
                    return null;
                } else {
                    DataNode dn = node.getData();
                    if( dn instanceof User ) {
                        User u = (User) dn;
                        if( log.isDebugEnabled() ) {
                            log.debug( "found user: " + u.getHref() );
                        }
                        return u;
                    } else {
                        log.warn( "Got a datanode, but its not a user: " + dn.getClass() );
                        return null;
                    }
                }
            }
        } else {
            // Attempt to authenticate against wrapped handler
            // If successful generate an authid and put into a request attribute
            AuthenticationHandler hnd = (AuthenticationHandler) request.getAttributes().get( "_delegatedAuthenticationHandler" );
            Object tag = hnd.authenticate( resource, request );
            if( tag != null ) {
                if( tag instanceof User) {
                    User user = (User) tag;
                    authId = generateAuthId();
                    log.info( "created authId: " + authId );
                    request.getAttributes().put( requestParamName, authId );
                    authIdMap.put( authId, user.getNameNodeId() );
                } else {
                    log.warn("auth.tag is not a user, is: " + tag);
                }
            }
            return tag;
        }
    }

    public String getChallenge( Resource resource, Request request ) {
        // doesnt do http challenge
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean isCompatible( Resource resource ) {
        // never issue challenge
        return false;
    }

    private String getAuthId( Request request ) {
        String authId = null;
        if(request.getParams() != null){
            authId = request.getParams().get( requestParamName );
        }
        if( authId == null ) {
            authId = getCookieValue( request );
        }
        if( authId != null ) {
            authId = authId.trim();
            if( authId.length() > 0 ) {
                return authId;
            }
        }
        return null;
    }

    private String getCookieValue( Request request ) {
        Cookie cookie = request.getCookie( cookieName );
        if( cookie == null ) return null;
        return cookie.getValue();
    }

    private String generateAuthId() {
        return UUID.randomUUID().toString();
    }
}
