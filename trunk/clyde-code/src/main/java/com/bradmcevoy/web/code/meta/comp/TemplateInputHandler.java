package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.TemplateInput;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class TemplateInputHandler implements ComponentHandler{

    private final TextHandler textHandler;

    public TemplateInputHandler( TextHandler textHandler ) {
        this.textHandler = textHandler;
    }
    
    public Class getComponentClass() {
        return TemplateInput.class;
    }

    public String getAlias() {
        return "eval";
    }

    public Element toXml( Component c ) {
        TemplateInput t = (TemplateInput) c;
        Element el = new Element(getAlias(),CodeMeta.NS);
        textHandler.populateXml( el, t );
        return el;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name");
        TemplateInput text = new TemplateInput(res, name );
        textHandler.fromXml( text, el );
        return text;
    }

}
