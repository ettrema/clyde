package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.IUser;

/**
 *
 * @author brad
 */
public interface ClydeAuthenticator {
    IUser authenticate( Resource resource, String user, String password );

    IUser authenticate( Resource resource, DigestResponse digestRequest );
}
