package com.ettrema.web.code.meta.comp;

import com.ettrema.web.Template;
import com.ettrema.web.component.ComponentDef;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public interface ComponentDefHandler {

    /**
     * The type of ComponentDef to handle
     * 
     * @return
     */
    Class getDefClass();

    String getAlias();

    Element toXml(ComponentDef def, Template template);

    ComponentDef fromXml( Template res, Element el );
}
