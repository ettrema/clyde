package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.TemplateInput;
import com.bradmcevoy.web.component.VelocityTemplateText;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class VelocityTemplateTextComponentHandler implements ComponentHandler{

    private final TextHandler textHandler;

    public VelocityTemplateTextComponentHandler( TextHandler textHandler ) {
        this.textHandler = textHandler;
    }

    public Class getComponentClass() {
        return VelocityTemplateText.class;
    }

    public String getAlias() {
        return "velocitytemplate";
    }

    public Element toXml( Component c ) {
        VelocityTemplateText t = (VelocityTemplateText) c;
        Element el = new Element(getAlias(),CodeMeta.NS);
        textHandler.populateXml( el, t );
        return el;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name");
        VelocityTemplateText text = new VelocityTemplateText(res, name );
        textHandler.fromXml( text, el );
        return text;
    }

}
