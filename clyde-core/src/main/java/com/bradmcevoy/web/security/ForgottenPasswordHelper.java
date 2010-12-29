package com.bradmcevoy.web.security;

import com.bradmcevoy.web.component.ForgottenPasswordComponent;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.User;
import com.ettrema.context.RequestContext;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessageImpl;
import com.ettrema.mail.send.MailSender;
import com.ettrema.vfs.VfsSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.mvel.TemplateInterpreter;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class ForgottenPasswordHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ForgottenPasswordHelper.class );
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_TOKEN = "token";
    private MailboxAddress parsedEmail;
    private String token;
    private String password; // the requested new password
    private User foundUser; // the located user

    public String onProcess( ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
        log.debug( "onProcess" );

        if( isResetRequest( parameters ) ) {
            return processReset( comp, rc, parameters );
        } else {
            return sendResetEmail( comp, rc, parameters, files );
        }
    }

    private String sendResetEmail( ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) throws IllegalArgumentException, NullPointerException {
        if( !validateEmailRequest( comp, rc, parameters ) ) {
            return null;
        }
        MailSender sender = RequestContext.getCurrent().get( MailSender.class );
        MailboxAddress to = parsedEmail;
        token = _( PasswordRecoveryService.class ).createToken( foundUser );
        String currentPassword = foundUser.getPassword( 847202 );
        String text = evalTemplate( (CommonTemplated) comp.getContainer(), comp.getBodyTemplate(), foundUser, token, currentPassword, parsedEmail.toPlainAddress() );
        String html = evalTemplate( (CommonTemplated) comp.getContainer(), comp.getBodyTemplateHtml(), foundUser, token, currentPassword, parsedEmail.toPlainAddress() );
        String rt = ( comp.getReplyTo() == null ) ? comp.getFromAdd() : comp.getReplyTo();
        if( StringUtils.isEmpty( comp.getFromAdd() ) ) {
            throw new IllegalArgumentException( "from address is null" );
        }
        if( rt == null ) {
            throw new IllegalArgumentException( "no reply to or from address" );
        }
        StandardMessageImpl sm = new StandardMessageImpl();
        MailboxAddress from = MailboxAddress.parse( comp.getFromAdd() );
        sm.setFrom( from );
        MailboxAddress replyTo = MailboxAddress.parse( rt );
        sm.setReplyTo( replyTo );
        sm.setTo( Arrays.asList( to ) );
        sm.setSubject( comp.getSubject() );
        if( !StringUtils.isEmpty( text ) ) {
            sm.setText( text );
        }
        if( !StringUtils.isEmpty( html ) ) {
            sm.setHtml( html );
        }
        sender.sendMail( sm );
        RequestParams.current().getAttributes().put( comp.getName() + "_confirmed", Boolean.TRUE ); // deprecated
        RequestParams.current().getAttributes().put( comp.getName() + "Confirmed", Boolean.TRUE );
        return checkRedirect( comp.getThankyouPage() );
    }

    private String evalTemplate( CommonTemplated container, String template, User user, String token, String password, String email ) {
        if( template == null ) {
            return null;
        }
        try {
            Map map = new HashMap();
            map.put( "user", user );
            map.put( "email", email );
            map.put( "token", token );
            map.put( "password", password );
            map.put( "targetPage", container );
            String s = TemplateInterpreter.evalToString( template, map );
            return s;
        } catch( Throwable e ) {
            log.error( "Exception rendering template: " + template, e );
            return "ERR";
        }

    }

    /**
     * Check that the request to send a reset email is valid
     *
     * @param rc
     * @return
     */
    private boolean validateEmailRequest( ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters ) {
        findUser( comp, parameters );
        if( foundUser != null ) {
            rc.addAttribute( comp.getName() + "_found", foundUser );
            return true;
        } else {
            return false;
        }
    }

    private void findUser( ForgottenPasswordComponent comp, Map<String, String> parameters ) {
        String email = parameters.get( "email" );
        try {
            // Parsing validates the email
            MailboxAddress add = MailboxAddress.parse( email );
            this.parsedEmail = add;
            foundUser = _( EmailAuthenticator.class ).findUser( add, (CommonTemplated) comp.getContainer() );

            if( foundUser != null ) {
                log.debug( "found user: " + foundUser.getName() );
            } else {
                comp.setValidationError( "No user accounts were found matching that address." );
                log.debug( "no users found" );
            }
        } catch( IllegalArgumentException e ) {
            log.debug( "invalid email address: error: " + email );
            comp.setValidationError( "Invalid email address. Please check the format, it should be like ben@somewhere.com" );
        }
    }

    /**
     * Check that the request to perform the password reset is valid
     *
     * @param rc
     * @return
     */
    private boolean validateResetRequest( ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters ) {
        token = parameters.get( PARAM_TOKEN );
        findUser( comp, parameters );
        if( foundUser == null ) {
            comp.setValidationError( "No user found" );
            log.trace( "user not found" );
            return false;
        }
        if( !_( PasswordRecoveryService.class ).isTokenValid( token, foundUser ) ) {
            log.trace( "token is not valid" );
            comp.setValidationError( "The requested token is not valid" );
            return false;
        }
        this.password = parameters.get( PARAM_PASSWORD );
        
        String passwordErr = _( PasswordValidationService.class ).checkValidity( foundUser, password );
        if( passwordErr != null ) {
            comp.setValidationError( passwordErr );
            return false;
        }
        log.trace( "reset request is valid" );
        return true;
    }

    /**
     * Determine if the request is for sending a reset email, or for actually
     * performing the reset
     *
     * @param parameters
     * @return - true if the request is to perform the actual reset of the user's password
     */
    private boolean isResetRequest( Map<String, String> parameters ) {
        return parameters.containsKey( PARAM_TOKEN );
    }

    private String processReset( ForgottenPasswordComponent comp, RenderContext rc, Map<String, String> parameters ) {
        if( !validateResetRequest( comp, rc, parameters ) ) {
            return null;
        }
        log.trace( "all ok, update password" );
        foundUser.setPassword( password, 847202 );
        foundUser.save();
        log.trace( "saved, now commit" );
        _( VfsSession.class ).commit();
        RequestParams.current().getAttributes().put( comp.getName() + "Changed", Boolean.TRUE );
        return checkRedirect( comp.getThankyouPage() );
    }

    private String checkRedirect( String thankyouPage ) {
        if( StringUtils.isNotBlank( thankyouPage ) ) {
            return thankyouPage;
        } else {
            return null;
        }
    }
}
