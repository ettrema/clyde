package com.ettrema.web.code.meta.comp;

import com.ettrema.web.component.AbstractInput;
import com.ettrema.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class AbstractInputHandler {

    public void fromXml( Element el, AbstractInput t ) {
        String sVal = InitUtils.getValue( el );
        Object o = t.parseValue( sVal );
        t.setValue( o );
    }

    public void populateXml( Element e2, AbstractInput t ) {
        e2.setAttribute( "name", t.getName() );
        if( t.isRequestScope() ) {
            InitUtils.setBoolean( e2, "requestScope", t.isRequestScope() );
        }
        if( t.isRequired() ) {
            InitUtils.setBoolean( e2, "required", t.isRequired() );
        }
        String s = t.getFormattedValue();
        if( s != null ) {
            if( s.contains( "<![CDATA" ) ) {
                e2.addContent( s );
            } else {
                //                CDATA data = new CDATA(s);
                //                e2.addContent(data);
                e2.setText( s );
            }
        }
    }
}
