package com.ettrema.web.security;

import com.ettrema.web.User;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author brad
 */
public class SimplePasswordValidationService implements PasswordValidationService {

    private int minLength = 6;

    /**
     * @{@inheritDoc}
     *
     * @param user
     * @param proposedPassword
     * @return
     */
    public String checkValidity( User user, String proposedPassword ) {
        if( StringUtils.isEmpty( proposedPassword )) {
            return "The password cannot be empty.";
        } else {
            String trimmed = proposedPassword.trim();
            if( !trimmed.equals( proposedPassword)) {
                return "The password cannot start or finish with spaces";
            } else {
                proposedPassword = trimmed;
                if( proposedPassword.length() < minLength) {
                    return "The password must be at least " + minLength + " characters long";
                } else {
                    return null;
                }
            }
        }
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength( int minLength ) {
        this.minLength = minLength;
    }

    

}
