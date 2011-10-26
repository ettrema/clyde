package com.bradmcevoy.web.security;

import com.bradmcevoy.web.User;

/**
 *
 * @author brad
 */
public interface PasswordRecoveryService {

    public String createToken( User user );

    public boolean isTokenValid( String token, User user );
}
