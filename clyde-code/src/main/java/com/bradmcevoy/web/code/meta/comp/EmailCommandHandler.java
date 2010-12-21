package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.EmailCommand;
import com.bradmcevoy.web.component.InitUtils;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author brad
 */
public class EmailCommandHandler implements ComponentHandler {

    private final CommandHandler commandHandler;

    public EmailCommandHandler( CommandHandler commandHandler ) {
        this.commandHandler = commandHandler;
    }

    public Class getComponentClass() {
        return EmailCommand.class;
    }

    public String getAlias() {
        return "singleEmail";
    }

    public Element toXml( Component c ) {
        EmailCommand g = (EmailCommand) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, g );
        return e2;
    }

    private void populateXml( Element e2, EmailCommand g ) {
        commandHandler.populateXml( e2, g );
        setText( e2, g.getTemplate().getValue() );
        //        InitUtils.addChild( e2, "html", bodyHtmlTemplate );
        InitUtils.setString( e2, "from", g.getFrom().getValue() );
        InitUtils.setString( e2, "to", g.getTo().getValue() );
        InitUtils.setString( e2, "subject", g.getSubject().getValue() );
        InitUtils.setString( e2, "confirmationUrl", g.getConfirmationUrl().getValue() );
        InitUtils.setString( e2, "replyTo", g.getReplyToTemplate().getValue() );
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        EmailCommand g = new EmailCommand( res, name );
        g.getTemplate().setValue( JDomUtils.valueOf( el, "template", CodeMeta.NS) );
        g.getFrom().setValue( InitUtils.getValue( el, "from" ) );
        g.getTo().setValue( InitUtils.getValue( el, "to" ) );
        g.getSubject().setValue( InitUtils.getValue( el, "subject" ) );
        g.getConfirmationUrl().setValue( InitUtils.getValue( el, "confirmationUrl" ) );
        g.getReplyToTemplate().setValue( InitUtils.getValue( el, "replyTo" ) );
        return g;
    }

    private void setText( Element el, String text ) {
        Element child = new Element( "template", CodeMeta.NS );
        child.setText( text );
        el.addContent( child );
    }
}
