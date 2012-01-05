package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.EmailInput;
import com.ettrema.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class EmailInputHandler implements ComponentHandler {

    private static final String ALIAS = "emailInput";
    private final TextHandler textHandler;

    public EmailInputHandler( TextHandler abstractInputHandler ) {
        this.textHandler = abstractInputHandler;
    }

    public Class getComponentClass() {
        return EmailInput.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml( Component c ) {
        EmailInput t = (EmailInput) c;
        Element e2 = new Element( ALIAS, CodeMeta.NS );
        populateXml( e2, t );
        return e2;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        EmailInput text = new EmailInput( res, name );
        fromXml( text, el );
        return text;
    }

    public void fromXml( EmailInput inp, Element el ) {
        textHandler.fromXml( inp, el );

    }

    public void populateXml( Element elThis, EmailInput t ) {
        textHandler.populateXml( elThis, t );
        InitUtils.set( elThis, "cols", t.cols );
    }
}
