package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.component.ComponentValue;
import org.jdom.Element;

/**
 * Handles serialization of ComponentValues
 *
 * @author brad
 */
public interface ValueHandler {

    /**
     * The class of ComponentValue this creates
     *
     * @return
     */
    Class getComponentValueClass();

    Element toXml(ComponentValue cv, CommonTemplated container);

    /**
     * The name of the element this handler creates
     *
     * @return
     */
    String getAlias();

    /**
     * Create a value object from the given xml and return it, but do not add it
     * to the parent
     *
     * @param res
     * @param eAtt
     * @return
     */
    ComponentValue fromXml(CommonTemplated res, Element eAtt);
}
