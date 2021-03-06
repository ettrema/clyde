package com.ettrema.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.ettrema.web.User;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class CookieAuthenticationHandler implements AuthenticationHandler, LogoutHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CookieAuthenticationHandler.class);
    private static final String HANDLER_ATT_NAME = "_delegatedAuthenticationHandler";
    private String requestParamName = "_clydeauthid";
    private String requestParamLogout = "_clydelogout";
    private String cookieAuthIdName = "_clydeauthid"; // TODO: make this a HTTP Only cookie, to avoid XSS attacks
    private String cookieUserUrl = "_clydeUser";
    private final List<AuthenticationHandler> handlers;
    private final Map<String, UUID> authIdMap = new ConcurrentHashMap<>();
    private List<CookieAuthEventHandler> eventHandlers;

    public CookieAuthenticationHandler(List<AuthenticationHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public boolean supports(Resource r, Request request) {
        // find the authId, if any, from the request
        String authId = getAuthId(request);

        // check for a logout command, if so logout

        if (isLogout(request)) {
            if (authId != null && authId.length() > 0) {
                log.trace("logout: authId: " + authId);
                authIdMap.remove(authId);
                clearCookieValue(HttpManager.response());
            }
        }

        for (AuthenticationHandler hnd : handlers) {
            if (hnd.supports(r, request)) {
                request.getAttributes().put(HANDLER_ATT_NAME, hnd);
                log.debug("supports: true: " + hnd.getClass().getCanonicalName());
                return true;
            }
        }

        // We will support it if there is either a auth id request param
        if (authId != null) {
            UUID id = authIdMap.get(authId);
            if (id != null) {
                log.debug("supports: found authId: " + authId);
                return true;
            } else {
                log.debug("supports: false, auth id given but not in map. Clear the cookie");
                clearCookieValue(HttpManager.response());
                return false;
            }
        } else {
            log.debug("supports: false");
            return false;
        }
    }

    @Override
    public Object authenticate(Resource resource, Request request) {
        log.trace("authenticate");
        // If there is a delegating handler which supports the request then we MUST use it
        AuthenticationHandler hnd = (AuthenticationHandler) request.getAttributes().get(HANDLER_ATT_NAME);
        if (hnd != null) {
            log.trace("delegating to: " + hnd);
            // Attempt to authenticate against wrapped handler
            // If successful generate an authid and put into a request attribute
            Object tag = hnd.authenticate(resource, request);
            if (tag != null) {
                if (tag instanceof User) {
                    setLoginCookies((User) tag, request);
                } else {
                    log.warn("auth.tag is not a user, is: " + tag);
                }
            }
            return tag;
        } else {
            String authId = getAuthId(request);
            if (authId == null) {
                log.trace("no authId in request");
                return null;
            } else {
                UUID id = authIdMap.get(authId);
                if (id == null) {
                    log.warn("Found authId, but no corresponding UUID");
                    return null;
                } else {
                    VfsSession session = RequestContext.getCurrent().get(VfsSession.class);
                    if (session == null) {
                        throw new IllegalStateException("no context");
                    }
                    NameNode node = session.get(id);
                    if (node == null) {
                        log.warn("Couldnt find node for id: " + id);
                        return null;
                    } else {
                        DataNode dn = node.getData();
                        if (dn == null) {
                            log.info("got a node with no data; " + id);
                            return null;
                        } else if (dn instanceof User) {
                            User u = (User) dn;
                            if (log.isDebugEnabled()) {
                                log.debug("found user: " + u.getHref());
                            }
                            return u;
                        } else {
                            log.warn("Got a datanode, but its not a user: " + dn.getClass());
                            return null;
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets cookies to make the given user the currently logged in user for any
     * subsequent requests.
     *
     * And also makes that user the current on-behalf-of user in
     * CurrentUserService
     *
     * @param user
     * @param request
     */
    public void setLoginCookies(User user, Request request) {
        if( request == null ) {
            return ;
        }
        String authId = generateAuthId(request);
        request.getAttributes().put(requestParamName, authId);
        authIdMap.put(authId, user.getNameNodeId());
        String userUrl = user.getHref();
        Response response = HttpManager.response();
        setCookieValue(response, authId, userUrl);

        // Need to make this the current user
        _(CurrentUserService.class).setOnBehalfOf(user);

        if (eventHandlers != null) {
            log.debug("process handlers");
            for (CookieAuthEventHandler h : eventHandlers) {
                log.debug("process event handler: " + h.getClass());
                h.afterAuthentication(request, response, user);
            }
        } else {
            log.trace("no event handlers");
        }
    }

    @Override
    public String getChallenge(Resource resource, Request request) {
        // doesnt do http challenge
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCompatible(Resource resource) {
        // never issue challenge
        return false;
    }

    private boolean isLogout(Request request) {
        if (request.getParams() == null) {
            return false;
        }

        String logoutCommand = request.getParams().get(requestParamLogout);
        return (logoutCommand != null && logoutCommand.length() > 0);
    }

    private String getAuthId(Request request) {
        if( request == null ) {
            return null;
        }
        String authId = null;
        if ( request.getParams() != null) {
            authId = request.getParams().get(requestParamName);
        }
        if (authId == null) {
            authId = getCookieValue(request);
        }
        if (authId != null) {
            authId = authId.trim();
            if (authId.length() > 0) {
                return authId;
            }
        }
        return null;
    }

    private String getCookieValue(Request request) {
        Cookie cookie = request.getCookie(cookieAuthIdName);
        if (cookie == null) {
            return null;
        }
        return cookie.getValue();
    }

    private void setCookieValue(Response response, String authId, String userUrl) {
        response.setCookie(cookieAuthIdName, authId);
        response.setCookie(cookieUserUrl, userUrl);
    }

    private void clearCookieValue(Response response) {
        response.setCookie(cookieAuthIdName, "");
        response.setCookie(cookieUserUrl, "");
    }

    private String generateAuthId(Request request) {
        String authId = getAuthId(request);
        if (authId != null && authIdMap.containsKey(authId)) {
            log.trace("use existing authid");
            return authId;
        } else {
            log.trace("generate new authid");
            return UUID.randomUUID().toString();
        }
    }

    public List<CookieAuthEventHandler> getEventHandlers() {
        return eventHandlers;
    }

    public void setEventHandlers(List<CookieAuthEventHandler> eventHandlers) {
        this.eventHandlers = eventHandlers;
    }

    public void logout(Request request, Auth auth) {
        log.trace("logout");
        String authId = getAuthId(request);
        if (authId == null) {
            log.trace("no authId in request");
        } else {
            log.trace("logout: removing:" + authId);
            authIdMap.remove(authId);
        }
    }
}
