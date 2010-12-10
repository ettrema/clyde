package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.GroupEmailCommand;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.xml.XmlHelper;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class GroupEmailCommandHandler implements ComponentHandler {

    private final CommandHandler commandHandler;

    public GroupEmailCommandHandler( CommandHandler commandHandler ) {
        this.commandHandler = commandHandler;
    }

    public Class getComponentClass() {
        return GroupEmailCommand.class;
    }

    public String getAlias() {
        return "groupEmail";
    }

    public Element toXml( Component c ) {
        GroupEmailCommand g = (GroupEmailCommand) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, g );
        return e2;
    }

    private void populateXml( Element e2, GroupEmailCommand g ) {
        commandHandler.populateXml( e2, g );
        InitUtils.addChild( e2, "c:text", g.getBodyTextTemplate() );
        setHtml( e2, g.getBodyHtmlTemplate() );
        //        InitUtils.addChild( e2, "html", bodyHtmlTemplate );
        InitUtils.setString( e2, "fromComp", g.getFromComp() );
        InitUtils.setString( e2, "toGroupExpr", g.getToGroupExpr() );
        InitUtils.setString( e2, "subjectComp", g.getSubjectComp() );
        InitUtils.setString( e2, "replyToComp", g.getReplyToComp() );
        InitUtils.setString( e2, "confirmationUrl", g.getConfirmationUrl() );
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        GroupEmailCommand g = new GroupEmailCommand( res, name );
        g.setBodyTextTemplate( InitUtils.getValueOf( el, "c:text" ) );
        g.setBodyHtmlTemplate( InitUtils.getValueOf( el, "c:html" ) );
        g.setFromComp( InitUtils.getValue( el, "fromComp"));
        g.setToGroupExpr( InitUtils.getValue( el, "toGroupExpr"));
        g.setSubjectComp( InitUtils.getValue( el, "subjectComp"));
        g.setReplyToComp( InitUtils.getValue( el, "replyToComp"));
        g.setConfirmationUrl( InitUtils.getValue( el, "confirmationUrl"));
        return g;
    }

    private void setHtml( Element el, String value ) {
        Element child = new Element( "html", DefaultValueHandler.NS_HTML_DEFAULT );
        XmlHelper helper = new XmlHelper();
        List content = helper.getContent( value );
        child.setContent( content );
        el.addContent( child );
    }
}
