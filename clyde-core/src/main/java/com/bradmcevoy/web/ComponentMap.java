package com.bradmcevoy.web;

import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.web.component.Addressable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.jdom.Element;

public class ComponentMap extends LinkedHashMap<String, Component> {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ComponentMap.class );
    private static final long serialVersionUID = 1L;

    public ComponentMap() {
    }

    public void add( Component c ) {
        put( c.getName(), c );
    }

    public Element toXml( Addressable container, Element el ) {
        if( this.size() > 0 ) {
            Element e2 = new Element( "components" );
            el.addContent( e2 );
            for( Component c : values() ) {
                if( !( c instanceof SystemComponent ) && !c.getName().equals( "name" ) ) { // name persisted as Text is deprecated
                    c.toXml( container, e2 );
                }
            }
            return e2;
        } else {
            return null;
        }
    }

    void addAll( ComponentMap componentMap ) {
        for( Component c : componentMap.values() ) {
            this.add( c );
        }
    }

    /**
     * Looks for an element called components and loads this from it
     * 
     * @param container
     * @param el
     */
    public void fromXml( Addressable container, Element el ) {
        Element e2 = el.getChild( "components" );
        _fromXml( container, e2 );
    }

    /**
     * Loads this map with all component elements under that given
     * 
     * @param container
     * @param el
     */
    public void _fromXml( Addressable container, Element el ) {
        this.clear();
        if( el != null ) {
            for( Object o : el.getChildren( "component" ) ) {
                Element eComp = (Element) o;
                add( container, eComp );
            }
        }
    }

    public void add( Addressable container, Element el ) {
        Component c = (Component) XmlUtils2.restoreObject( el, container );
        if( c.getName().equals( "name" ) ) return;
        add( c );
    }

    public List getList() {
        ArrayList list = new ArrayList();
        list.addAll( values() );
        return list;
    }

    void init( Addressable container ) {
        for( Component c : this.values() ) {
            c.init( container );
        }
    }
}
