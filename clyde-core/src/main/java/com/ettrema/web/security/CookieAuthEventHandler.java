package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;

/**
 *
 * @author brad
 */
public interface CookieAuthEventHandler {
    void afterAuthentication(Request request, Response response, Object tag);
}
