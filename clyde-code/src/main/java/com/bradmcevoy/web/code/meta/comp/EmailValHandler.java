package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.EmailVal;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class EmailValHandler implements ValueHandler {

    private final DefaultValueHandler defaultValueHandler;

    public EmailValHandler( DefaultValueHandler defaultValueHandler ) {
        this.defaultValueHandler = defaultValueHandler;
    }

    public Class getComponentValueClass() {
        return EmailVal.class;
    }

    public Element toXml( ComponentValue cv, CommonTemplated container ) {
        Element el = new Element(getAlias(), CodeMeta.NS);
        populateXml(el, (EmailVal) cv, container);
        return el;
    }

    private void populateXml( Element e2, EmailVal cv, CommonTemplated container ) {
        defaultValueHandler.populateXml( e2, cv, container );
    }    

    public String getAlias() {
        return "email";
    }

    public ComponentValue fromXml( CommonTemplated res, Element eAtt ) {
        String name = eAtt.getAttributeValue( "name");
        EmailVal val = new EmailVal( name, res);
        defaultValueHandler.fromXml(eAtt, res, val );
        return val;
    }

}
