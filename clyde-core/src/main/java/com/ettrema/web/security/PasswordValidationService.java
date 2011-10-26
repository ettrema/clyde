package com.ettrema.web.security;

import com.ettrema.web.User;

/**
 * Just checks to see if a password is valid, and if not reports the reason
 *
 * @author brad
 */
public interface PasswordValidationService {
    /**
     * Return the localised error message if the password is invalid, otherwise
     * return null
     *
     * @param user
     * @param proposedPassword
     * @return
     */
    String checkValidity(User user, String proposedPassword);
}
