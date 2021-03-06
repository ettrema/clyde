package com.ettrema.web.code.meta.comp;

import com.ettrema.web.Template;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.PasswordDef;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class PasswordDefHandler implements ComponentDefHandler {

    private final static String ALIAS = "password";

    public Element toXml(ComponentDef def, Template template) {
        PasswordDef ddef = (PasswordDef) def;
        Element el = new Element(ALIAS, CodeMeta.NS);
        populateXml(el, ddef);
        return el;
    }

    private void populateXml(Element el, PasswordDef ddef) {
        el.setAttribute("name", ddef.getName());
    }

    public Class getDefClass() {
        return PasswordDef.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public PasswordDef fromXml(Template res, Element el) {
        String name = el.getAttributeValue("name");
        PasswordDef def = new PasswordDef(res, name);
        return def;
    }
}
