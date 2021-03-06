package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.DateVal;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class DateValueHandler implements ValueHandler {

    private final DefaultValueHandler defaultValueHandler;

    public DateValueHandler( DefaultValueHandler defaultValueHandler ) {
        this.defaultValueHandler = defaultValueHandler;
    }

    public Class getComponentValueClass() {
        return DateVal.class;
    }

    public Element toXml( ComponentValue cv, CommonTemplated container ) {
        Element el = new Element( getAlias(), CodeMeta.NS );
        populateXml( el, (DateVal) cv, container );
        return el;
    }

    private void populateXml( Element e2, DateVal cv, CommonTemplated container ) {
        defaultValueHandler.populateXml( e2, cv, container );
    }

    public String getAlias() {
        return "date";
    }

    public ComponentValue fromXml( CommonTemplated res, Element eAtt ) {
        String name = eAtt.getAttributeValue( "name" );
        DateVal val = new DateVal( name, res );
        defaultValueHandler.fromXml( eAtt, res, val );
        return val;
    }
}
