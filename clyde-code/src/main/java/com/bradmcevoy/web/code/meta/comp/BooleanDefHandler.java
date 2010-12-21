package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.BooleanDef;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.DateDef;
import com.bradmcevoy.web.component.InitUtils;
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
