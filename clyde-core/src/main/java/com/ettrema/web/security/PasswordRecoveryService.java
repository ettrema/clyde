package com.ettrema.web.security;

import com.ettrema.web.User;

/**
 *
 * @author brad
 */
public interface PasswordRecoveryService {

    public String createToken( User user );

    public boolean isTokenValid( String token, User user );
}
