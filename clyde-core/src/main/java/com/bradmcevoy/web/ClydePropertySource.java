package com.bradmcevoy.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.property.PropertySource;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ClydePropertySource implements PropertySource{

    private static final Logger log = LoggerFactory.getLogger( ClydePropertySource.class );

    public Object getProperty(QName name, Resource r) {
        if( r instanceof Templatable ) {
            if( isClydeNs(name)) {
                Templatable t = (Templatable) r;
                ComponentValue v = t.getValues().get(name.getLocalPart());
                if( v == null ) {
                    return null;
                } else {
                    return v.getValue();
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void setProperty(QName name, Object value, Resource r) {
        ITemplate template;
        ComponentDef def;
        if( r instanceof Templatable ) {
            if( isClydeNs(name)) {
                Templatable t = (Templatable) r;
                ComponentValue v = t.getValues().get(name.getLocalPart());
                if( v == null ) {
                    template = t.getTemplate();
                    if( template != null ) {
                        def = template.getComponentDef(name.getLocalPart());
                        if( def != null ) {
                            v = def.createComponentValue(t);
                            t.getValues().add(v);
                            v.setValue(value);
                        } else {
                            throw new RuntimeException("no such component def: " + name.getLocalPart());
                        }
                    }
                } else {
                    v.setValue(value);
                }
            } else {
                throw new RuntimeException("not a clyde ns");
            }

        } else {
            throw new RuntimeException("Unsupported type: " + r.getClass());
        }

    }

    public PropertyMetaData getPropertyMetaData(QName name, Resource r) {
        if( r instanceof Templatable ) {
            Templatable t = (Templatable) r;
            ITemplate template = t.getTemplate();
            if( isClydeNs(name)) {
                if( template != null ) {
                    ComponentDef def = template.getComponentDef(name.getLocalPart());
                    if( def != null ) {
                        return new PropertyMetaData(PropertyAccessibility.WRITABLE, def.getValueClass());
                    } else {
                        return PropertyMetaData.UNKNOWN;
                    }
                } else {
                    log.warn("no tempalte for: " + t.getHref());
                    return PropertyMetaData.UNKNOWN;
                }
            } else {
                return PropertyMetaData.UNKNOWN;
            }
        } else {
            return PropertyMetaData.UNKNOWN;
        }
    }

    public void clearProperty(QName name, Resource r) {
        if( r instanceof Templatable ) {
            Templatable t = (Templatable) r;
        } else {
            throw new RuntimeException("Unsupported type: " + r.getClass());
        }
    }

    public List<QName> getAllPropertyNames(Resource r) {
        if( r instanceof Templatable ) {
            Templatable t = (Templatable) r;
            ITemplate template = t.getTemplate();
            List<QName> list = new ArrayList<QName>();
            if( template != null ) {
                for( ComponentDef def : template.getComponentDefs().values()) {
                    QName qname = new QName("clyde", def.getName());
                    list.add(qname);
                }
            }
            return list;
        } else {
            return null;
        }
    }

    private boolean isClydeNs(QName name) {
        return name.getNamespaceURI() != null && name.getNamespaceURI().equals("clyde");
    }

}