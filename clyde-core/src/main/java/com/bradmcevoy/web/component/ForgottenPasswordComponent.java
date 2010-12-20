package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.EmailAddress;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.User;
import com.ettrema.context.RequestContext;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessageImpl;
import com.ettrema.mail.send.MailSender;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
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
    private String thankyouPage;

    public ForgottenPasswordComponent( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }


    public ForgottenPasswordComponent( Addressable container, Element el ) {
        this.container = container;
        fromXml( el );
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
            // Parsing validates the email
            MailboxAddress add = MailboxAddress.parse( email );
            RequestParams.current().attributes.put( "parsedEmail", add );
            VfsSession vfs = RequestContext.getCurrent().get( VfsSession.class );
            List<NameNode> list = vfs.find( EmailAddress.class, email );
            if( list == null || list.isEmpty() ) {
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
        if( subject == null )
            throw new NullPointerException( "subject is null" );
        if( fromAdd == null )
            throw new NullPointerException( "from Address is null" );

        if( !validate( rc ) ) {
            log.debug( "not valid" );
            return null;
        }
        MailSender sender = RequestContext.getCurrent().get( MailSender.class );
        List<User> list = (List<User>) rc.getAttribute( name + "_found" );
        MailboxAddress to = (MailboxAddress) RequestParams.current().attributes.get( "parsedEmail" );
        for( User user : list ) {
            String password = user.getPassword( 847202 );
            String text = evalTemplate( user, password );
            if( text == null ) {
                throw new NullPointerException( "Template evaluated to null" );
            }
            String rt = ( replyTo == null ) ? fromAdd : replyTo;
            StandardMessageImpl sm = new StandardMessageImpl();
            sm.setFrom( MailboxAddress.parse( fromAdd ) );
            sm.setReplyTo( MailboxAddress.parse( rt ) );
            sm.setTo( Arrays.asList( to ) );
            sm.setSubject( subject );
            sm.setText( text );
            sender.sendMail( sm );
        }
        RequestParams.current().getAttributes().put( name + "_confirmed", Boolean.TRUE );
        if( thankyouPage != null && thankyouPage.length() > 0 ) {
            return thankyouPage;
        } else {
            return null;
        }
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

    public final void fromXml( Element el ) {
        name = el.getAttributeValue( "name" );
        fromAdd = el.getAttributeValue( "from" );
        replyTo = el.getAttributeValue( "replyTo" );
        thankyouPage = el.getAttributeValue( "thankyouPage" );
        subject = el.getChildText( "subject" );
        bodyTemplate = el.getChildText( "body" );        
    }


    public void populateXml( Element e2 ) {
        e2.setAttribute( "class", getClass().getName() );
        populateXml_NoClass( e2 );
    }

    public void populateXml_NoClass( Element e2 ) {
        e2.setAttribute( "name", name );
        InitUtils.setString( e2, "from", fromAdd );
        InitUtils.setString( e2, "replyTo", replyTo );
        InitUtils.setString( e2, "thankyouPage", thankyouPage );

        Element elSubject = new Element( "subject" );
        e2.addContent( elSubject );
        elSubject.setText( subject );

        Element elBody = new Element( "body" );
        e2.addContent( elBody );
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

    public String getFromAdd() {
        return fromAdd;
    }

    public void setFromAdd( String fromAdd ) {
        this.fromAdd = fromAdd;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo( String replyTo ) {
        this.replyTo = replyTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject( String subject ) {
        this.subject = subject;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate( String bodyTemplate ) {
        this.bodyTemplate = bodyTemplate;
    }

    public String getThankyouPage() {
        return thankyouPage;
    }

    public void setThankyouPage( String thankyouPage ) {
        this.thankyouPage = thankyouPage;
    }
}
