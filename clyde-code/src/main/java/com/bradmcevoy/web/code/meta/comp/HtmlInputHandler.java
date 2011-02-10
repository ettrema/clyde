package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.HtmlInput;
import com.bradmcevoy.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class HtmlInputHandler implements ComponentHandler{

    private final TextHandler textHandler;

    public HtmlInputHandler( TextHandler textHandler ) {
        this.textHandler = textHandler;
    }
    
    public Class getComponentClass() {
        return HtmlInput.class;
    }

    public String getAlias() {
        return "htmlInput";
    }

    public Element toXml( Component c ) {
        HtmlInput html = (HtmlInput) c;
        Element el = new Element(getAlias(),CodeMeta.NS);
        textHandler.populateXml( el, html );
        InitUtils.set(el, "disAllowTemplating", html.isDisAllowTemplating());
        return el;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name");
        HtmlInput html = new HtmlInput(res, name );
        textHandler.fromXml( html, el );
        html.setDisAllowTemplating(InitUtils.getBoolean(el, "disAllowTemplating"));
        return html;
    }

}
