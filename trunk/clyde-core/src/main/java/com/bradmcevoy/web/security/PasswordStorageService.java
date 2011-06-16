package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.http11.auth.DigestGenerator;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.property.PropertySource.PropertySetException;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.Text;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Implements getting and setting of passwords, and convenience methods for
 * performing authentication
 *
 * Supports the old style of password storage which was to store a Text component
 * on the user, as well as the newer style which is to rely on component definitions
 * to set component values
 *
 * Also supports access key authentication
 * 
 * TODO: probably should split these into 3 seperate composable services
 *
 * @author brad
 */
public class PasswordStorageService {

    public static final String PASSWORD_NAME = "password";
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PasswordStorageService.class );
    private final PasswordValidationService passwordValidationService;

    public PasswordStorageService( PasswordValidationService passwordValidationService ) {
        this.passwordValidationService = passwordValidationService;
    }

    public boolean checkPasswordMD5( User user, byte[] passwordHash ) {
        if( user.isAccountDisabled()) {
            log.info("account is disabled: " + user.getHref());
            return false;
        }

        try {
            String s = getPasswordValue( user );
            MessageDigest digest = java.security.MessageDigest.getInstance( "MD5" );
            byte[] actual = digest.digest( s.getBytes() );
            return java.util.Arrays.equals( actual, passwordHash );
        } catch( NoSuchAlgorithmException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public boolean checkPassword( User user, String password ) {
        if( user.isAccountDisabled()) {
            log.info("account is disabled: " + user.getHref());
            return false;
        }
        String actualPassword = getPasswordValue( user );
        boolean b = actualPassword.equals( password );
        if( !b ) {
            log.trace( "passwords don't match: " + user.getHref() );
        } else {
            return b;
        }

        // No match found, so check for accessKey
        List<String> accessKeys = user.getAccessKeys();
        if( accessKeys != null ) {
            for( String s : accessKeys ) {
                if( s.equals( password ) ) {
                    log.trace( "found matching accesskey" );
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkPassword( User user, DigestResponse digestRequest ) {
        if( user.isAccountDisabled()) {
            log.info("account is disabled: " + user.getHref());
            return false;
        }

        String actualPassword = getPasswordValue( user );

        DigestGenerator digestGenerator = new DigestGenerator();
        String serverDigest = digestGenerator.generateDigest( digestRequest, actualPassword );
        boolean b = serverDigest.equals( digestRequest.getResponseDigest() );
        if( !b ) {
            log.warn( "digest checkPassword failed: " + user.getPath() + "/" + actualPassword );
        }
        return b;

    }

    /**
     * First look for a component value and use it if exists.
     * If not, look for the locally defined Text component
     *
     * @return
     */
    public String getPasswordValue( User user ) {
        ComponentValue cv = user.getValues().get( PasswordStorageService.PASSWORD_NAME );
        if( cv != null ) {
            Object oVal = cv.getValue();
            if( oVal == null ) {
                return "";
            } else {
                return oVal.toString();
            }
        } else {
            if( user.passwordComponent() != null ) {
                return user.passwordComponent().getValue();
            } else {
                return "";
            }
        }
    }

    public void setPasswordValue( User user, String newPassword ) {
        String validationErr = passwordValidationService.checkValidity( user, newPassword );
        ComponentValue cv;
        if( validationErr == null ) {
            log.trace( "setPassword: validation ok" );
            ComponentDef cdef = getDef( user, PasswordStorageService.PASSWORD_NAME );
            if( cdef != null ) {
                cv = user.getValues().get( cdef.getName() );
                if( cv == null ) {
                    cv = cdef.createComponentValue( user );
                    user.getValues().add( cv );
                }
                cv.setValue( newPassword );
            } else {
                if( user.passwordComponent() == null ) {
                    user.setPasswordComponent( new Text( user, PasswordStorageService.PASSWORD_NAME ) );
                    user.getComponents().add( user.passwordComponent() );
                }
                user.passwordComponent().setValue( newPassword );
            }
        } else {
            log.info( "setPassword: validation failed: " + validationErr );
            throw new PropertySetException( Status.SC_BAD_REQUEST, validationErr );
        }

    }

    private ComponentDef getDef( User user, String name ) {
        ITemplate template = user.getTemplate();
        if( template == null ) {
            return null;
        } else {
            return template.getComponentDefs().get( name );
        }
    }
}
