package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.TemplateInput;
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
