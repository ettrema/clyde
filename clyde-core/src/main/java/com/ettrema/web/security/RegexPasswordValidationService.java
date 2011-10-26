package com.ettrema.web.security;

import com.ettrema.web.Component;
import com.ettrema.web.User;
import com.ettrema.web.component.Text;

/**
 *
 * @author brad
 */
public class RegexPasswordValidationService implements PasswordValidationService {

    private final PasswordValidationService wrapped;

    private String defaultRegex = "((?=.*\\d)(?=.*[A-z]).{6,})";

    private String validationMessage = "Passwords must be composed of at least one digit and 5 letters ";

    public RegexPasswordValidationService() {
        wrapped = null;
    }

    public RegexPasswordValidationService( PasswordValidationService wrapped ) {
        this.wrapped = wrapped;
    }


    public String checkValidity( User user, String proposedPassword ) {
        if( wrapped != null ) {
            String msg = wrapped.checkValidity( user, proposedPassword );
            if( msg != null ) {
                return msg;
            }
        }
        Component c = user.getComponent( "passwordRegex" );
        String regex = defaultRegex;
        if( c != null ) {
            if( c instanceof Text ) {
                regex = ( (Text) c ).getValue();
            }
        }
        if( !matches(regex, proposedPassword) ) {
            return validationMessage;
        } else {
            return null;
        }
    }

    public boolean matches( String regex, String proposedPassword ) {
        if( proposedPassword == null ) {
            return false;
        } else {
            return proposedPassword.matches( regex );
        }
    }

    public String getDefaultRegex() {
        return defaultRegex;
    }

    public void setDefaultRegex( String defaultRegex ) {
        this.defaultRegex = defaultRegex;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage( String validationMessage ) {
        this.validationMessage = validationMessage;
    }

    
}
