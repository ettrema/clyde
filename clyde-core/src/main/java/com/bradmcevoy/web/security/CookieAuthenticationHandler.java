package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.web.User;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author brad
 */
public class CookieAuthenticationHandler implements AuthenticationHandler, LogoutHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CookieAuthenticationHandler.class );
    private static final String HANDLER_ATT_NAME = "_delegatedAuthenticationHandler";
    private String requestParamName = "_clydeauthid";
    private String cookieAuthIdName = "_clydeauthid";
    private String cookieUserUrl = "_clydeUser";
    private final List<AuthenticationHandler> handlers;
    private final Map<String, UUID> authIdMap = new ConcurrentHashMap<String, UUID>();
    private List<CookieAuthEventHandler> eventHandlers;

    public CookieAuthenticationHandler( List<AuthenticationHandler> handlers ) {
        this.handlers = handlers;
    }

    public boolean supports( Resource r, Request request ) {
        for( AuthenticationHandler hnd : handlers ) {
            if( hnd.supports( r, request ) ) {
                request.getAttributes().put( HANDLER_ATT_NAME, hnd );
                log.debug( "supports: true: " + hnd.getClass().getCanonicalName() );
                return true;
            }
        }

        // We will support it if there is either a auth id request param, or a cookie
        String authId = getAuthId( request );
        if( authId != null ) {
            UUID id = authIdMap.get( authId );
            if( id != null ) {
                log.debug( "supports: found authId: " + authId );
                return true;
            } else {
                log.debug( "supports: false, auth id not in map");
                return false;
            }
        } else {
            log.debug( "supports: false" );
            return false;
        }
    }

    public Object authenticate( Resource resource, Request request ) {
        log.trace( "authenticate" );
        // If there is a delegating handler which supports the request then we MUST use it
        AuthenticationHandler hnd = (AuthenticationHandler) request.getAttributes().get( HANDLER_ATT_NAME );
        if( hnd != null ) {
            log.trace( "delegating to: " + hnd );
            // Attempt to authenticate against wrapped handler
            // If successful generate an authid and put into a request attribute
            Object tag = hnd.authenticate( resource, request );
            if( tag != null ) {
                if( tag instanceof User ) {
                    User user = (User) tag;
                    String authId = generateAuthId();
                    log.info( "authenticated ok. created authId: " + authId );
                    request.getAttributes().put( requestParamName, authId );
                    authIdMap.put( authId, user.getNameNodeId() );
                    String userUrl = user.getHref();
                    Response response = HttpManager.response();
                    setCookieValue( response, authId, userUrl );
                    if( eventHandlers != null ) {
                        log.debug( "process handlers");
                        for( CookieAuthEventHandler h : eventHandlers ) {
                            log.debug("process event handler: " + h.getClass());
                            h.afterAuthentication( request, response, tag );
                        }
                    } else {
                        log.trace("no event handlers");
                    }
                } else {
                    log.warn( "auth.tag is not a user, is: " + tag );
                }
            }
            return tag;
        } else {
            String authId = getAuthId( request );
            if( authId == null ) {
                log.trace( "no authId in request" );
                return null;
            } else {
                UUID id = authIdMap.get( authId );
                if( id == null ) {
                    log.warn( "Found authId, but no corresponding UUID" );
                    return null;
                } else {
                    VfsSession session = RequestContext.getCurrent().get( VfsSession.class );
                    if( session == null ) {
                        throw new IllegalStateException( "no context" );
                    }
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
            }
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
        if( request.getParams() != null ) {
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
        Cookie cookie = request.getCookie( cookieAuthIdName );
        if( cookie == null ) return null;
        return cookie.getValue();
    }

    private void setCookieValue( Response response, String authId, String userUrl ) {
        response.setCookie( cookieAuthIdName, authId );
        response.setCookie( cookieUserUrl, userUrl );
    }

    private String generateAuthId() {
        return UUID.randomUUID().toString();
    }

    public List<CookieAuthEventHandler> getEventHandlers() {
        return eventHandlers;
    }

    public void setEventHandlers( List<CookieAuthEventHandler> eventHandlers ) {
        this.eventHandlers = eventHandlers;
    }

    public void logout( Request request, Auth auth ) {
        log.trace("logout");
        String authId = getAuthId( request );
        if( authId == null ) {
            log.trace( "no authId in request" );
        } else {
            log.trace("logout: removing:" + authId );
            authIdMap.remove( authId );
        }
    }
}
