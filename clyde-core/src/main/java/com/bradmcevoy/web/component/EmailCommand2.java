package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.ettrema.mail.MailServer;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessage;
import com.ettrema.mail.StandardMessageImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import org.jdom.Element;

public class EmailCommand2 extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( EmailCommand.class );
    private static final long serialVersionUID = 1L;
    private String templateComp;
    private String fromComp;
    private String toComp;
    private String subjectComp;
    private String replyToComp;
    private String confirmationUrl;

    public EmailCommand2( Addressable container, String name ) {
        super( container, name );
    }

    public EmailCommand2( Addressable container, Element el ) {
        super( container, el );
        templateComp = InitUtils.getValue( el, "templateComp" );
        fromComp = InitUtils.getValue( el, "fromComp" );
        toComp = InitUtils.getValue( el, "toComp" );
        subjectComp = InitUtils.getValue( el, "subjectComp" );
        replyToComp = InitUtils.getValue( el, "replyToComp" );
        confirmationUrl = InitUtils.getValue( el, "confirmationUrl" );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.setString( e2, "templateComp", templateComp );
        InitUtils.setString( e2, "fromComp", fromComp );
        InitUtils.setString( e2, "toComp", toComp );
        InitUtils.setString( e2, "subjectComp", subjectComp );
        InitUtils.setString( e2, "replyToComp", replyToComp );
        InitUtils.setString( e2, "confirmationUrl", confirmationUrl );
    }

    @Override
    public String renderEdit( RenderContext rc ) {
        return "todo";
    }

    private MailboxAddress getFrom( RenderContext rc ) {
        Component c = getFromComponent();
        String s = c.render( rc );
        return MailboxAddress.parse( s );
    }

    private Component getFromComponent() {
        return getParentComponent( this.fromComp );
    }

    private List<MailboxAddress> getTo( RenderContext rc ) {
        Component c = getToComponent();
        String s = c.render( rc );
        List<MailboxAddress> list = new ArrayList<MailboxAddress>();
        list.add( MailboxAddress.parse( s ) );
        return list;
    }

    public String getEmailBody( RenderContext rc ) {
        Component c = getTemplateComponent();
        rc.invoke( c );
        String s = c.render( rc );
        return s;
    }

    private Component getReplyToComponent() {
        return getParentComponent( this.replyToComp );
    }

    private String getSubject( RenderContext rc ) {
        Component c = getSubjectComponent();
        if( c == null ) return "not found: " + this.subjectComp;
        String s = c.render( rc );
        return s;
    }

    public MailboxAddress getReplyTo( RenderContext rc ) {
        Component c = getReplyToComponent();
        String s = c.render( rc );
        return MailboxAddress.parse( s );
    }

    @Override
    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        String s = parameters.get( this.getName() );
        if( s == null ) {
            return null; // not this command
        }
        if( !validate( rc ) ) {
            log.debug( "validation failed" );
            return null;
        }
        return doProcess( rc, parameters, files );
    }

    @Override
    protected String doProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        try {
            send( rc, files );
            return confirmationUrl;
        } catch( MessagingException ex ) {
            log.error( "exception sending email", ex );
            return null;
        }
    }

    @Override
    public boolean validate( RenderContext rc ) {
        return true;
    }

    public void send( RenderContext rc, Map<String, FileItem> files ) throws MessagingException {
        log.debug( "send: files: " + files.size() );
        StandardMessage sm = new StandardMessageImpl();
        sm.setText( getEmailBody( rc ) );
        sm.setSubject( getSubject( rc ) );
        sm.setTo( getTo( rc ) );
        sm.setFrom( getFrom( rc ) );
        sm.setReplyTo( getReplyTo( rc ) );

        if( files != null ) {
            for( FileItem f : files.values() ) {
                if( f.getName() != null && f.getName().trim().length() > 0 ) {
                    log.debug( "adding attachment: " + f.getName() + " - " + f.getContentType() );
                    sm.addAttachment( f.getName(), f.getContentType(), null, f.getInputStream() );
                } else {
                    log.warn( "not adding empty attachment" );
                }
            }
        }

        MailServer mailServer = requestContext().get( MailServer.class );
        mailServer.getMailSender().sendMail( sm );
    }

    @Override
    public Path getPath() {
        return container.getPath().child( name );
    }

    private Component getSubjectComponent() {
        return getParentComponent( this.subjectComp );
    }

    private Component getParentComponent( String name ) {
        CommonTemplated ct = (CommonTemplated) this.getContainer();
        log.debug( "getParentComponent:: " + name + " - on " + ct.getName() );
        Component c = ct.getAnyComponent( name );
        return c;
    }

    private Component getTemplateComponent() {
        return getParentComponent( this.templateComp );
    }

    private Component getToComponent() {
        return getParentComponent( this.toComp );
    }
}
