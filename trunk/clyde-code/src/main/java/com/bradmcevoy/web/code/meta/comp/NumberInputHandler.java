package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.NumberInput;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class NumberInputHandler implements ComponentHandler {

    private static final String ALIAS = "number";
    private final AbstractInputHandler abstractInputHandler;

    public NumberInputHandler( AbstractInputHandler abstractInputHandler ) {
        this.abstractInputHandler = abstractInputHandler;
    }

    public Class getComponentClass() {
        return NumberInput.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml( Component c ) {
        NumberInput t = (NumberInput) c;
        Element e2 = new Element( ALIAS, CodeMeta.NS );
        populateXml( e2, t );
        return e2;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        NumberInput text = new NumberInput( res, name );
        fromXml( text, el );
        return text;
    }

    public void fromXml( NumberInput inp, Element el ) {
        inp.cols = InitUtils.getInt( el, "cols" );
    }

    public void populateXml( Element elThis, NumberInput t ) {
        abstractInputHandler.populateXml( elThis, t );
        InitUtils.set( elThis, "cols", t.cols );
    }
}
