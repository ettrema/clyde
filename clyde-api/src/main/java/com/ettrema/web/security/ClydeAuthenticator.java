package com.ettrema.web.security;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.web.IUser;

/**
 *
 * @author brad
 */
public interface ClydeAuthenticator {
    IUser authenticate( Resource resource, String user, String password );

    IUser authenticate( Resource resource, DigestResponse digestRequest );
}
