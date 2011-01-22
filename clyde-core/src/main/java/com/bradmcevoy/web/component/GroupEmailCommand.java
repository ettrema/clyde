package com.bradmcevoy.web.component;

import org.mvel.MVEL;
import com.bradmcevoy.web.velocity.VelocityInterpreter;
import com.bradmcevoy.web.IUser;
import org.apache.velocity.VelocityContext;
import java.util.Arrays;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Group;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.UserGroup;
import com.ettrema.mail.MailServer;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.groups.GroupService;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessage;
import com.ettrema.mail.StandardMessageImpl;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 * An email command which sends to a group, rather then a particular user
 *
 * @author brad
 * @deprecated - use GroupEmailCommand2 instead
 */
public class GroupEmailCommand extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( GroupEmailCommand.class );
    private static final long serialVersionUID = 1L;
    private String bodyTextTemplate;
    private String bodyHtmlTemplate;
    private String fromComp;
    private String toGroupExpr;
    private String subjectComp;
    private String replyToComp;
    private String confirmationUrl;

    public GroupEmailCommand( Addressable container, String name ) {
        super( container, name );
    }

    public GroupEmailCommand( Addressable container, Element el ) {
        super( container, el );
        // Note: these fields identify the names of components on the parent
        // resource
        bodyTextTemplate = InitUtils.getChild( el, "text" );
        Element elHtml = el.getChild( "html" );
        if( elHtml != null ) {
            bodyHtmlTemplate = InitUtils.getValue( elHtml );
        } else {
            bodyHtmlTemplate = null;
        }
        fromComp = InitUtils.getValue( el, "fromComp" );
        toGroupExpr = InitUtils.getValue( el, "toGroupExpr" );
        subjectComp = InitUtils.getValue( el, "subjectComp" );
        replyToComp = InitUtils.getValue( el, "replyToComp" );
        confirmationUrl = InitUtils.getValue( el, "confirmationUrl" );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.addChild( e2, "text", bodyTextTemplate );
        InitUtils.setElementString(e2, "html", bodyHtmlTemplate);
//        InitUtils.addChild( e2, "html", bodyHtmlTemplate );
        InitUtils.setString( e2, "fromComp", fromComp );
        InitUtils.setString( e2, "toGroupExpr", toGroupExpr );
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

    private List<User> getTo( RenderContext rc ) {
        Group group = getGroup( rc );
        return group.getMembers();
    }

    private MailboxAddress getAddress( User user ) {
        String sEmail = user.getExternalEmailTextV2( "default" );
        if( sEmail != null && sEmail.length() > 0 ) {
            MailboxAddress add = null;
            try {
                add = MailboxAddress.parse( sEmail );
                return add;
            } catch( IllegalArgumentException e ) {
                log.error( "Couldnt parse: " + sEmail, e );
                return null;
            }
        } else {
            return null;
        }
    }

    private Group getGroup( RenderContext rc ) {
        Object o = MVEL.eval( toGroupExpr, rc.getTargetPage() );
        if( o == null ) {
            throw new RuntimeException( "Expression returned null, should have returned group or name of group" );
        } else if( o instanceof String ) {
            GroupService groupService = _( GroupService.class );
            String groupName = (String) o;
            UserGroup group = groupService.getGroup( (Resource) this.getContainer(), groupName );
            if( group == null ) {
                throw new RuntimeException( "Unknown group: " + groupName );
            }
            if( group instanceof Group ) {
                Group g = (Group) group;
                return g;
            } else {
                throw new RuntimeException( "Group " + groupName + " is not an appropriate type. Is a: " + group.getClass() + " - but must be a: " + Group.class );
            }

        } else if( o instanceof Group ) {
            return (Group) o;
        } else {
            throw new RuntimeException( "Un-supported group type: " + o.getClass().getName() );
        }
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
        log.trace( "onProcess" );
        String s = parameters.get( this.getName() );
        if( s == null ) {
            return null; // not this command
        }
        log.trace( "onProcess2" );
        if( !validate( rc ) ) {
            log.debug( "validation failed" );
            return null;
        }
        return doProcess( rc, parameters, files );
    }

    @Override
    protected String doProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        log.trace( "doProcess" );
        try {
            send( rc );
            if( confirmationUrl != null && confirmationUrl.length() > 0 ) {
                return confirmationUrl;
            } else {
                return null;
            }
        } catch( MessagingException ex ) {
            log.error( "exception sending email", ex );
            return null;
        }
    }

    @Override
    public boolean validate( RenderContext rc ) {
        return true;
    }

    public void send( RenderContext rc ) throws MessagingException {
        log.debug( "send" );
        List<User> recipList = getTo( rc );
        MailServer mailServer = requestContext().get( MailServer.class );
        for( User user : recipList ) {
            StandardMessage sm = new StandardMessageImpl();
            sm.setText( getBody( rc, user, bodyTextTemplate ) );
            sm.setHtml( getBody( rc, user, bodyHtmlTemplate ) );
            sm.setSubject( getSubject( rc ) );
            MailboxAddress address = getAddress( user );
            sm.setTo( Arrays.asList( address ) );
            sm.setFrom( getFrom( rc ) );
            sm.setReplyTo( getReplyTo( rc ) );
            mailServer.getMailSender().sendMail( sm );
        }
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

    private String getBody( RenderContext rc, IUser user, String template ) {
        if( template == null ) {
            return null;
        }
        Path path = CommonComponent.getPath( this, rc );
        VelocityContext vc = CommonComponent.velocityContext( rc, name, path, user );
        try {
            return VelocityInterpreter.evalToString( template, vc );
        } catch( Throwable e ) {
            log.error( "Exception rendering template: " + template, e );
            return "ERR";
        }
    }

    public String getBodyTextTemplate() {
        return bodyTextTemplate;
    }

    public void setBodyTextTemplate( String bodyTextTemplate ) {
        this.bodyTextTemplate = bodyTextTemplate;
    }

    public String getBodyHtmlTemplate() {
        return bodyHtmlTemplate;
    }

    public void setBodyHtmlTemplate( String bodyHtmlTemplate ) {
        this.bodyHtmlTemplate = bodyHtmlTemplate;
    }

    public String getFromComp() {
        return fromComp;
    }

    public void setFromComp( String fromComp ) {
        this.fromComp = fromComp;
    }

    public String getToGroupExpr() {
        return toGroupExpr;
    }

    public void setToGroupExpr( String toGroupExpr ) {
        this.toGroupExpr = toGroupExpr;
    }

    public String getSubjectComp() {
        return subjectComp;
    }

    public void setSubjectComp( String subjectComp ) {
        this.subjectComp = subjectComp;
    }

    public String getReplyToComp() {
        return replyToComp;
    }

    public void setReplyToComp( String replyToComp ) {
        this.replyToComp = replyToComp;
    }

    public String getConfirmationUrl() {
        return confirmationUrl;
    }

    public void setConfirmationUrl( String confirmationUrl ) {
        this.confirmationUrl = confirmationUrl;
    }
}
