package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
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
