package com.ettrema.web.code.meta.comp;

import com.ettrema.web.Template;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.GroupsDef;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class GroupsDefHandler  implements ComponentDefHandler{

    private final static String ALIAS = "groupsDef";

    private final TextDefHandler textDefHandler;

    public GroupsDefHandler( TextDefHandler textDefHandler ) {
        this.textDefHandler = textDefHandler;
    }

    public Element toXml( ComponentDef def, Template template ) {
        GroupsDef ddef = (GroupsDef) def;
        Element el = new Element(ALIAS, CodeMeta.NS);
        populateXml( el, ddef);
        return el;
    }

    private void populateXml( Element el, GroupsDef ddef ) {
        textDefHandler.populateXml( el, ddef );
    }

    public Class getDefClass() {
        return GroupsDef.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml( Template res, Element el ) {
        String name = el.getAttributeValue( "name");
        GroupsDef def = new GroupsDef( res, name );
        textDefHandler.fromXml( el, def );
        return def;
    }

}
