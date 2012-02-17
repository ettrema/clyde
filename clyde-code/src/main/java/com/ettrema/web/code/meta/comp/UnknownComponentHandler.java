package com.ettrema.web.code.meta.comp;

import com.bradmcevoy.utils.XmlUtils2;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.meta.BaseResourceMetaHandler;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author HP
 */
public class UnknownComponentHandler implements ComponentHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BaseResourceMetaHandler.class);

    @Override
    public Class getComponentClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAlias() {
        return "c:unknown";
    }

    @Override
    public Element toXml(Component c) {
        Element el = new Element("unknown", CodeMeta.NS);
        c.toXml(c.getContainer(), el);

        return el;
    }

    @Override
    public Component fromXml(CommonTemplated res, Element el) {
        Element elComp = el.getChild("component");
        log.warn("Attempting to restore unknown component: " + elComp.toString());        
        Component c = (Component) XmlUtils2.restoreObject(elComp, res);
        return (Component) c;
    }
}
