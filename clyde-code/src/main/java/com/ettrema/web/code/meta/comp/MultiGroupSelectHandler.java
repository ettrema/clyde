package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.GroupSelect;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.MultiGroupSelect;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class MultiGroupSelectHandler implements ComponentHandler {

    @Override
    public Class getComponentClass() {
        return MultiGroupSelect.class;
    }

    @Override
    public String getAlias() {
        return "multiGroupSelect";
    }

    @Override
    public Element toXml( Component c ) {
        MultiGroupSelect t = (MultiGroupSelect) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, t );
        return e2;
    }

    @Override
    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        if(StringUtils.isEmpty( name )) {
            throw new RuntimeException( "Empty component name");
        }
        MultiGroupSelect text = new MultiGroupSelect( res, name );
        fromXml( text, el );
        return text;
    }

    public void fromXml( MultiGroupSelect text, Element el ) {

    }

    public void populateXml( Element elThis, MultiGroupSelect t ) {
        InitUtils.set( elThis, "name", t.getName() );
    }
}
