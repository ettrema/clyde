package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.GroovyCommand;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class GroovyCommandHandler implements ComponentHandler {

    public Class getComponentClass() {
        return GroovyCommand.class;
    }

    public String getAlias() {
        return "groovy";
    }

    public Element toXml(Component c) {
        GroovyCommand t = (GroovyCommand) c;
        Element el = new Element(getAlias(), CodeMeta.NS);
        el.setAttribute("name", t.getName());
        el.setText(t.getScript());
        return el;
    }

    public Component fromXml(CommonTemplated res, Element el) {
        String name = el.getAttributeValue("name");
        GroovyCommand text = new GroovyCommand(res, name);
        text.setScript(el.getText());
        return text;
    }
}
