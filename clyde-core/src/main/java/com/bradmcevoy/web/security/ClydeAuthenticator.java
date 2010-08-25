package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.User;

/**
 *
 * @author brad
 */
public interface ClydeAuthenticator {
    User authenticate( Resource resource, String user, String password );

    User authenticate( Resource resource, DigestResponse digestRequest );
}
