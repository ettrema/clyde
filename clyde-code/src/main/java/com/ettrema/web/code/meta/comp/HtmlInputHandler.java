package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.HtmlInput;
import com.ettrema.web.component.InitUtils;
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
