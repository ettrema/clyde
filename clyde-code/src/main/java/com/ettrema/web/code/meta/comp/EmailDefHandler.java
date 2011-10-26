package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.EmailDef;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class EmailDefHandler implements ComponentDefHandler{

    private final static String ALIAS = "email";

    private final TextDefHandler textDefHandler;

    public EmailDefHandler( TextDefHandler textDefHandler ) {
        this.textDefHandler = textDefHandler;
    }

    public Element toXml( ComponentDef def, Template template ) {
        EmailDef ddef = (EmailDef) def;
        Element el = new Element(ALIAS, CodeMeta.NS);
        populateXml( el, ddef);
        return el;
    }

    private void populateXml( Element el, EmailDef ddef ) {
        textDefHandler.populateXml( el, ddef );
    }

    public Class getDefClass() {
        return EmailDef.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml( Template res, Element el ) {
        String name = el.getAttributeValue( "name");
        EmailDef def = new EmailDef( res, name);
        textDefHandler.fromXml( el, def );
        return def;
    }
    
}
