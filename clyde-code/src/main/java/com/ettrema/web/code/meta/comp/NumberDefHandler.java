package com.ettrema.web.code.meta.comp;

import com.ettrema.web.Template;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.NumberDef;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class NumberDefHandler implements ComponentDefHandler {

    private final static String ALIAS = "numberDef";
    private final TextDefHandler textDefHandler;

    public NumberDefHandler(TextDefHandler textDefHandler) {
        this.textDefHandler = textDefHandler;
    }

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml(Template res, Element el) {
        String name = el.getAttributeValue("name");
        NumberDef def = new NumberDef(res, name);
        fromXml(el, def);
        return def;
    }

    public Element toXml(ComponentDef def, Template template) {
        NumberDef html = (NumberDef) def;
        Element el = new Element(ALIAS, CodeMeta.NS);
        populateXml(el, html);
        return el;
    }

    public Class getDefClass() {
        return NumberDef.class;
    }

    public void fromXml(Element el, NumberDef def) {
        def.setDecimals(InitUtils.getInt(el, "decimals"));
        textDefHandler.fromXml(el, def);
    }

    private void populateXml(Element el, NumberDef def) {
        InitUtils.set(el, "decimals", def.getDecimals());
        textDefHandler.populateXml(el, def);
    }
}
