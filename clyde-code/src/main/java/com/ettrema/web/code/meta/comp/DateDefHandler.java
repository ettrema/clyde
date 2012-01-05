package com.ettrema.web.code.meta.comp;

import com.ettrema.web.Template;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.DateDef;
import com.ettrema.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class DateDefHandler implements ComponentDefHandler{

    private final static String ALIAS = "date";

    private final TextDefHandler textDefHandler;

    public DateDefHandler( TextDefHandler textDefHandler ) {
        this.textDefHandler = textDefHandler;
    }

    public Element toXml( ComponentDef def, Template template ) {
        DateDef ddef = (DateDef) def;
        Element el = new Element(ALIAS, CodeMeta.NS);
        populateXml( el, ddef);
        return el;
    }

    private void populateXml( Element el, DateDef ddef ) {
        textDefHandler.populateXml( el, ddef );
        InitUtils.set( el, "hasTime", ddef.getShowTime() );
    }

    public Class getDefClass() {
        return DateDef.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml( Template res, Element el ) {
        String name = el.getAttributeValue( "name");
        DateDef def = new DateDef( res, name );
        def.setShowTime(InitUtils.getBoolean(el, "hasTime"));
        textDefHandler.fromXml( el, def );
        return def;
    }
    
}
