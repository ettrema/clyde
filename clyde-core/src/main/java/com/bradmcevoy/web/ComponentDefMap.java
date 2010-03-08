package com.bradmcevoy.web;

import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentDef;
import java.util.LinkedHashMap;
import org.jdom.Element;

public class ComponentDefMap extends LinkedHashMap<String,ComponentDef> {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ComponentDefMap.class);
    
    private static final long serialVersionUID = 1L;
    
    public void add( ComponentDef p ) {
        put(p.getName(), p);        
    }    

    public Element toXml(Addressable container, Element el) {
        Element e2 = new Element("componentDefs");
        el.addContent(e2);
        for( ComponentDef cdef : values() ) {
            cdef.toXml( container,e2 );
        }
        return e2;
    }

    public void fromXml(Addressable container, Element el) {
        this.clear();
        Element e2 = el.getChild("componentDefs");
        if( e2 == null ) {
            log.warn("no componentDefs element found");
            return ;
        }
        log.debug("loading component defs from xml");
        for( Object o : e2.getChildren("componentDef")) {
            Element elCDef = (Element)o;
            ComponentDef def = (ComponentDef) XmlUtils2.restoreObject(elCDef,container);
            add( def );
        }
    }

    public void addAll(ComponentDefMap componentDefs) {
        for( ComponentDef def : componentDefs.values() ) {
            this.add(def);
        }
    }

    void init(Template parent) {
        for( ComponentDef def : this.values() ) {
            def.init(parent);
        }
    }
}
