package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public interface ComponentHandler {
    Class getComponentClass();

    String getAlias();

    Element toXml(Component c);

    Component fromXml(CommonTemplated res, Element el);
}
