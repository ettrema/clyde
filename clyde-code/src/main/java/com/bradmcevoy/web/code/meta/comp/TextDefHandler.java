package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.TextDef;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class TextDefHandler implements ComponentDefHandler {

    private final static String ALIAS = "text";

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml(Template res, Element el) {
        String name = el.getAttributeValue("name");
        TextDef def = new TextDef(res, name);
        fromXml(el, def);
        return def;
    }

    public Element toXml(ComponentDef def, Template template) {
        TextDef html = (TextDef) def;
        Element el = new Element(ALIAS, CodeMeta.NS);
        populateXml(el, html);
        return el;
    }

    public void populateXml(Element el, TextDef text) {
        InitUtils.set(el, "name", text.getName());
        InitUtils.set(el, "rows", text.getRowsVal());
        InitUtils.set(el, "cols", text.getColsVal());
        InitUtils.set(el, "required", text.isRequired());
        InitUtils.set(el, "description", text.getDescription());
        InitUtils.set(el, "disAllowTemplating", text.isDisAllowTemplating());
        InitUtils.setList(el, "choices", text.getChoices());
    }

    public Class getDefClass() {
        return TextDef.class;
    }

    public void fromXml(Element el, TextDef def) {
        def.setRowsVal(InitUtils.getInteger(el, "rows"));
        def.setColsVal(InitUtils.getInteger(el, "cols"));
        def.setRequired(InitUtils.getBoolean(el, "required"));
        def.setDescription(InitUtils.getValue(el, "description"));
        def.setDisAllowTemplating(InitUtils.getBoolean(el, "disAllowTemplating"));
        def.setChoices(InitUtils.getList(el, "choices"));
    }
}
