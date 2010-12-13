package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.GroupSelect;
import com.bradmcevoy.web.component.InitUtils;
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
        Element e2 = new Element(getAlias(), CodeMeta.NS);
        populateXml( e2, t );
        return e2;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name");
        GroupSelect text = new GroupSelect(res, name );
        fromXml( text, el );
        return text;
    }

    public void fromXml( GroupSelect text, Element el ) {
        text.setGroupName( InitUtils.getValue( el, "group" ) );
    }

    public void populateXml( Element elThis, GroupSelect t ) {
        InitUtils.set(elThis, "name", t.getName());
        InitUtils.set(elThis, "group", t.getGroupName());

    }
}
