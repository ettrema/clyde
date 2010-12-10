package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.component.Command;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CommandHandler {
    public void populateXml(Element e2, Command cmd) {
        e2.setAttribute("name",cmd.getName());
    }
}
