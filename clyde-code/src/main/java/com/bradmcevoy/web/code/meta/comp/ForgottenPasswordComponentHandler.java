package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.ForgottenPasswordComponent;
import com.bradmcevoy.web.component.InitUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class ForgottenPasswordComponentHandler implements ComponentHandler {

    public Class getComponentClass() {
        return ForgottenPasswordComponent.class;
    }

    public String getAlias() {
        return "forgottenPassword";
    }

    public Element toXml( Component c ) {
        ForgottenPasswordComponent t = (ForgottenPasswordComponent) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, t );
        return e2;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        if(StringUtils.isEmpty( name )) {
            throw new RuntimeException( "Empty component name");
        }
        ForgottenPasswordComponent text = new ForgottenPasswordComponent( res, name );
        fromXml( text, el );
        return text;
    }

    public void fromXml( ForgottenPasswordComponent text, Element el ) {
        text.setFromAdd( el.getAttributeValue( "from" ) );
        text.setReplyTo( el.getAttributeValue( "replyTo" ) );
        text.setThankyouPage( el.getAttributeValue( "thankyouPage" ) );
        text.setSubject( JDomUtils.valueOf( el, "subject", CodeMeta.NS) );
        text.setBodyTemplate( JDomUtils.valueOf( el, "body", CodeMeta.NS) );
    }

    public void populateXml( Element elThis, ForgottenPasswordComponent t ) {
        InitUtils.set( elThis, "name", t.getName() );
        InitUtils.set( elThis, "from", t.getFromAdd() );
        InitUtils.set( elThis, "replyTo", t.getReplyTo() );
        InitUtils.set( elThis, "thankyouPage", t.getThankyouPage() );
        JDomUtils.setChild( elThis, "subject", t.getSubject(), CodeMeta.NS);
        JDomUtils.setChild( elThis, "body", t.getBodyTemplate(), CodeMeta.NS);

    }
}
