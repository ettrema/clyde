package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;

/**
 *
 * @author brad
 */
public interface LogoutHandler {

    void logout( Request request, Auth auth );

}
