package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.GroupSelect;
import com.ettrema.web.component.InitUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class GroupSelectHandler implements ComponentHandler {

    public Class getComponentClass() {
        return GroupSelect.class;
    }

    public String getAlias() {
        return "groupSelect";
    }

    public Element toXml( Component c ) {
        GroupSelect t = (GroupSelect) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, t );
        return e2;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        if(StringUtils.isEmpty( name )) {
            throw new RuntimeException( "Empty component name");
        }
        GroupSelect text = new GroupSelect( res, name );
        fromXml( text, el );
        return text;
    }

    public void fromXml( GroupSelect text, Element el ) {
        text.setGroupName( InitUtils.getValue( el, "group" ) );
    }

    public void populateXml( Element elThis, GroupSelect t ) {
        InitUtils.set( elThis, "name", t.getName() );
        InitUtils.set( elThis, "group", t.getGroupName() );

    }
}
