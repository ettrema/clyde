package com.ettrema.web.code.meta.comp;

import com.ettrema.utils.JDomUtils;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.ForgottenPasswordComponent;
import com.ettrema.web.component.InitUtils;
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
        if( StringUtils.isEmpty( name ) ) {
            throw new RuntimeException( "Empty component name" );
        }
        ForgottenPasswordComponent text = new ForgottenPasswordComponent( res, name );
        fromXml( text, el );
        return text;
    }

    public void fromXml( ForgottenPasswordComponent fpc, Element el ) {
        fpc.setFromAdd( el.getAttributeValue( "from" ) );
        fpc.setReplyTo( el.getAttributeValue( "replyTo" ) );
        fpc.setThankyouPage( el.getAttributeValue( "thankyouPage" ) );
        fpc.setRecaptchaComponent( InitUtils.getValue( el, "recaptcha") );

        fpc.setSubject( JDomUtils.valueOf( el, "subject", CodeMeta.NS ) );

        String body = JDomUtils.valueOf( el, "bodyText", CodeMeta.NS );
        fpc.setBodyTemplate( body );
        String html =JDomUtils.valueOf( el, "bodyHtml", CodeMeta.NS );
        fpc.setBodyTemplateHtml( html );
        fpc.setUseToken( InitUtils.getBoolean( el, "useToken" ) );
    }

    public void populateXml( Element elThis, ForgottenPasswordComponent t ) {
        InitUtils.set( elThis, "name", t.getName() );
        InitUtils.set( elThis, "from", t.getFromAdd() );
        InitUtils.set( elThis, "replyTo", t.getReplyTo() );
        InitUtils.set( elThis, "thankyouPage", t.getThankyouPage() );
        InitUtils.set( elThis, "useToken", t.isUseToken() );
        InitUtils.set( elThis, "recaptcha", t.getRecaptchaComponent() );
        JDomUtils.setChildText( elThis, "subject", t.getSubject(), CodeMeta.NS );
        JDomUtils.setChildText( elThis, "bodyText", t.getBodyTemplate(), CodeMeta.NS );
        JDomUtils.setChildXml( elThis, "bodyHtml", t.getBodyTemplateHtml(), CodeMeta.NS );

    }
}
