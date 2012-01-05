package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.captcha.ReCaptchaComponent;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.InitUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class ReCaptchaComponentHandler  implements ComponentHandler {

    public Class getComponentClass() {
        return ReCaptchaComponent.class;
    }

    public String getAlias() {
        return "recaptcha";
    }

    public Element toXml( Component c ) {
        ReCaptchaComponent t = (ReCaptchaComponent) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, t );
        return e2;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        if( StringUtils.isEmpty( name ) ) {
            throw new RuntimeException( "Empty component name" );
        }
        ReCaptchaComponent text = new ReCaptchaComponent( res, name );
        fromXml( text, el );
        return text;
    }

    private void populateXml( Element elThis, ReCaptchaComponent cap ) {
        InitUtils.set( elThis, "name", cap.getName() );
        InitUtils.set( elThis, "privateKey", cap.getPrivateKey() );
        InitUtils.set( elThis, "publicKey", cap.getPublicKey() );
    }

    public void fromXml( ReCaptchaComponent cap, Element e2 ) {
        cap.setPrivateKey( InitUtils.getValue( e2, "privateKey"));
        cap.setPublicKey( InitUtils.getValue( e2, "publicKey"));
    }

}
