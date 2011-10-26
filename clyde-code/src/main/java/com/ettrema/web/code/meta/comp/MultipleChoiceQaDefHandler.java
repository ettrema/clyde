package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.forms.MultipleChoiceQaDef;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class MultipleChoiceQaDefHandler implements ComponentDefHandler {

    private final static String ALIAS = "multiplechoiceqa";

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml( Template res, Element el ) {
        String name = el.getAttributeValue( "name" );
        MultipleChoiceQaDef def = new MultipleChoiceQaDef( res, name ); 
        fromXml( el, def );
        return def;
    }

    public Element toXml( ComponentDef def, Template template ) {
        MultipleChoiceQaDef html = (MultipleChoiceQaDef) def;
        Element el = new Element( ALIAS, CodeMeta.NS );
        populateXml( el, html );
        return el;
    }

    public void populateXml( Element el, MultipleChoiceQaDef text ) {
        InitUtils.set( el, "name", text.getName() );
    }

    public Class getDefClass() {
        return MultipleChoiceQaDef.class;
    }

    public void fromXml( Element el, MultipleChoiceQaDef def ) {
    }
}
