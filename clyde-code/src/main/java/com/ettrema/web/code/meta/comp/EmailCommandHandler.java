package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.EmailCommand3;
import org.jdom.Element;

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
        return EmailCommand3.class;
    }

    public String getAlias() {
        return "singleEmail";
    }

    public Element toXml( Component c ) {
        EmailCommand3 g = (EmailCommand3) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, g );
        return e2;
    }

    private void populateXml( Element e2, EmailCommand3 g ) {
        commandHandler.populateXml( e2, g );
        g.populateLocalXml(e2);
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        EmailCommand3 g = new EmailCommand3( res, name );
        //String text = InitUtils.getValueOf( el, "c:text" );
        g.parseXml(el);
        return g;
    }

}
