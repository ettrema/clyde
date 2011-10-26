package com.ettrema.web.code.meta.comp;

import com.ettrema.web.Template;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.BooleanDef;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.DateDef;
import com.ettrema.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class BooleanDefHandler implements ComponentDefHandler {

    private final static String ALIAS = "boolean";
    private final TextDefHandler textDefHandler;

    public BooleanDefHandler( TextDefHandler textDefHandler ) {
        this.textDefHandler = textDefHandler;
    }

    public Element toXml( ComponentDef def, Template template ) {
        BooleanDef ddef = (BooleanDef) def;
        Element el = new Element( ALIAS, CodeMeta.NS );
        populateXml( el, ddef );
        return el;
    }

    private void populateXml( Element el, BooleanDef ddef ) {
        textDefHandler.populateXml( el, ddef );
        InitUtils.set( el, "type", ddef.getType() );
    }

    public Class getDefClass() {
        return BooleanDef.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml( Template res, Element el ) {
        String name = el.getAttributeValue( "name" );
        BooleanDef def = new BooleanDef( res, name );
        def.setType( InitUtils.getValue( el, "type" ) );
        textDefHandler.fromXml( el, def );
        return def;
    }
}
