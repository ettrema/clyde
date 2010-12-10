package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.HtmlDef;
import com.bradmcevoy.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class HtmlDefHandler implements ComponentDefHandler {

    private final static String ALIAS = "html";
    private final TextDefHandler textDefHandler;

    public HtmlDefHandler( TextDefHandler textDefHandler ) {
        this.textDefHandler = textDefHandler;
    }

    public Element toXml( ComponentDef def, Template template ) {
        HtmlDef html = (HtmlDef) def;
        Element el = new Element( ALIAS, CodeMeta.NS );
        populateXml( el, html );
        return el;
    }

    private void populateXml( Element el, HtmlDef html ) {
        textDefHandler.populateXml( el, html );
        InitUtils.set( el, "toolbar", html.getToolbarSetName() );
    }

    public Class getDefClass() {
        return HtmlDef.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml( Template res, Element el ) {
        String name = el.getAttributeValue( "name" );
        HtmlDef def = new HtmlDef( res, name );
        textDefHandler.fromXml( el, def );
        def.setToolbarSetName( InitUtils.getValue( el, "toolbar" ) );
        return def;
    }
}
