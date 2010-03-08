package com.bradmcevoy.web;

import com.bradmcevoy.utils.ReflectionUtils;
import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import java.util.LinkedHashMap;
import org.jdom.Element;

public class ComponentValueMap extends LinkedHashMap<String, ComponentValue> {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ComponentValueMap.class );

    private static final long serialVersionUID = 1L;

    public void add( ComponentValue parameterValue ) {
        put( parameterValue.name, parameterValue );
    }

    public ComponentValue create( Addressable page, String name, String value ) {
        ComponentValue pv = new ComponentValue( name, value );
        pv.init( page );
        add( pv );
        return pv;
    }

    public Element toXml( Addressable container, Element el ) {
        log.debug( "************ to xml");
        if( this.size() > 0 ) {
            Element e2 = new Element( "componentValues" );
            el.addContent( e2 );
            for( ComponentValue cv : values() ) {
                cv.toXml( container, e2 );
            }
            return e2;
        } else {
            return null;
        }
    }

    public void fromXml( Element el, CommonTemplated container ) {
        log.debug( "fromXml");
        this.clear();
        if( el == null ) return;

        Element e2 = el.getChild( "componentValues" );
        if( e2 == null ) return;

        for( Element eCV : XmlUtils2.children( e2, "componentValue" ) ) {
            String clazz = InitUtils.getValue( eCV, "class", ComponentValue.class.getName() );
//            log.debug("create CV: " + clazz + " - " + eCV.getAttributeValue( "class"));
            ComponentValue cv = (ComponentValue) ReflectionUtils.create( clazz, eCV, container );
            add( cv );
            //add( new ComponentValue(eCV,container) ); 
        }
    }

    void addAll( ComponentValueMap valueMap ) {
        for( ComponentValue cv : valueMap.values() ) {
            this.add( cv );
        }
    }

    void init( Addressable parent ) {
        for( ComponentValue cv : this.values() ) {
            cv.init( parent );
        }
    }
}
