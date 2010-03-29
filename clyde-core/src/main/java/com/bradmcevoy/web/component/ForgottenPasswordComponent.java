package com.bradmcevoy.web.component;

import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.EmailAddress;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.User;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.send.MailSender;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import org.mvel.TemplateInterpreter;

/**
 *
 * @author brad
 */
public class ForgottenPasswordComponent implements Component {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ForgottenPasswordComponent.class );
    private static final long serialVersionUID = 1L;
    private String name;
    private Addressable container;
    private String fromAdd;
    private String replyTo;
    private String subject;
    private String bodyTemplate;

    public ForgottenPasswordComponent( Addressable container, Element el ) {
        this.container = container;
        fromXml( el );
    }

    public void fromXml( Element el ) {
        name = el.getAttributeValue( "name" );
        fromAdd = el.getAttributeValue( "from" );
        replyTo = el.getAttributeValue( "replyTo" );
        subject = el.getChildText( "subject" );
        bodyTemplate = el.getChildText( "body" );
    }

    public void init( Addressable container ) {
        this.container = container;
    }

    public Addressable getContainer() {
        return container;
    }

    public boolean validate( RenderContext rc ) {
        String email = RequestParams.current().getParameters().get( "email" );
        try {
            MailboxAddress add = MailboxAddress.parse( email );
            VfsSession vfs = RequestContext.getCurrent().get( VfsSession.class );
            List<NameNode> list = vfs.find( EmailAddress.class, email );
            if( list == null || list.size() == 0 ) {
                log.debug( "no nodes found" );
                setValidationError( "That email address wasn't found." );
                return false;
            } else {
                List<User> foundUsers = new ArrayList<User>();
                for( NameNode node : list ) {
                    NameNode nUser = node.getParent().getParent(); // the first parent is just a holder
                    DataNode dnUser = nUser.getData();
                    if( dnUser != null && dnUser instanceof User ) {
                        User user = (User) dnUser;
                        foundUsers.add( user );
                    } else {
                        log.warn( "parent is not a user: " + dnUser.getClass() );
                    }
                }
                if( foundUsers.size() > 0 ) {
                    log.debug( "is valid" );
                    rc.addAttribute( name + "_found", foundUsers );
                    return true;
                } else {
                    setValidationError( "No user accounts were found matching that address." );
                    log.debug( "no users found" );
                    return false;
                }
            }
        } catch( IllegalArgumentException e ) {
            log.debug( "invalid email address: error: " + email );
            setValidationError( "Invalid email address. Please check the format, it should be like ben@somewhere.com" );
            return false;
        }
    }

    public String render( RenderContext rc ) {
        return "";
    }

    public String renderEdit( RenderContext rc ) {
        return "";
    }

    public String getName() {
        return name;
    }

    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
        log.debug( "onProcess" );
        if( !validate( rc ) ) {
            log.debug( "not valid" );
            return null;
        }
        MailSender sender = RequestContext.getCurrent().get( MailSender.class );
        String email = (String) rc.getAttribute( name + "_email" );
        List<User> list = (List<User>) rc.getAttribute( name + "_found" );
        List<String> to = Arrays.asList( email );
        for( User user : list ) {
            String password = user.getPassword( 847202 );
            String text = evalTemplate( user, password );
            sender.sendMail( fromAdd, null, to, replyTo, subject, text );
        }
        RequestParams.current().getAttributes().put( name + "_confirmed", Boolean.TRUE );
        return null;
    }

    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        String email = parameters.get( "email" );
        log.debug( "email: " + email );
        rc.addAttribute( name + "_email", email );
    }

    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "component" );
        el.addContent( e2 );
        populateXml( e2 );
        return e2;
    }

    private void populateXml( Element e2 ) {
        e2.setAttribute( "class", getClass().getName() );
        e2.setAttribute( "name", name );

        InitUtils.setString( e2, "from", fromAdd );
        InitUtils.setString( e2, "replyTo", replyTo );
        Element elContent = e2.addContent( new Element( "subject" ) );
        elContent.setText( subject );
        Element elBody = e2.addContent( new Element( "body" ) );
        elBody.setText( bodyTemplate );

    }

    private String evalTemplate( User user, String password ) {
        try {
            Map map = new HashMap();
            map.put( "user", user );
            map.put( "password", password );
            String s = TemplateInterpreter.evalToString( bodyTemplate, map );
            return s;
        } catch( Throwable e ) {
            log.error( "Exception rendering template: " + bodyTemplate, e );
            return "ERR";
        }

    }

    public void setValidationError( String s ) {
        RequestParams.current().getAttributes().put( name + "_error", s );
    }

    public String getValidationError() {
        return (String) RequestParams.current().getAttributes().get( name + "_error" );
    }
}
