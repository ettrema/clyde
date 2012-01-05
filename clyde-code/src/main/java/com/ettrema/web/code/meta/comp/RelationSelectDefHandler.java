package com.ettrema.web.code.meta.comp;

import com.ettrema.web.Template;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.RelationSelectDef;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class RelationSelectDefHandler implements ComponentDefHandler {

    private final static String ALIAS = "relation";

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml( Template res, Element el ) {
        String name = el.getAttributeValue( "name" );
        RelationSelectDef def = new RelationSelectDef( res, name );
        fromXml( el, def );
        return def;
    }

    public Element toXml( ComponentDef def, Template template ) {
        RelationSelectDef html = (RelationSelectDef) def;
        Element el = new Element( ALIAS, CodeMeta.NS );
        populateXml( el, html );
        return el;
    }

    public void populateXml( Element el, RelationSelectDef text ) {
        InitUtils.set( el, "name", text.getName() );
        InitUtils.set( el, "relation", text.getRelationName() );
        InitUtils.set( el, "required", text.isRequired() );
        InitUtils.set( el, "description", text.getDescription() );
        InitUtils.set( el, "selectFromFolder", text.getSelectFromFolder() );
        InitUtils.set( el, "selectTemplate", text.getSelectTemplate() );
    }

    public Class getDefClass() {
        return RelationSelectDef.class;
    }

    public void fromXml( Element el, RelationSelectDef def ) {
        def.setRequired( InitUtils.getBoolean( el, "required" ) );
        def.setDescription( InitUtils.getValue( el, "description" ) );
        def.setRelationName( InitUtils.getValue( el, "relation" ) );
        def.setSelectFromFolder( InitUtils.getValue( el, "selectFromFolder" ) );
        def.setSelectTemplate( InitUtils.getValue( el, "selectTemplate" ) );
    }
}
