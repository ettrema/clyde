package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.Text;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class TextHandler implements ComponentHandler{

    private static final String ALIAS = "text";

    private final AbstractInputHandler abstractInputHandler;

    public TextHandler( AbstractInputHandler abstractInputHandler ) {
        this.abstractInputHandler = abstractInputHandler;
    }
    
    public Class getComponentClass() {
        return Text.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml( Component c ) {
        Text t = (Text) c;
        Element e2 = new Element(ALIAS, CodeMeta.NS);
        populateXml( e2, t );
        return e2;
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name");
        Text text = new Text(res, name );
        fromXml( text, el );
        return text;
    }

    public void fromXml( Text text, Element el ) {
        text.setRows( InitUtils.getInt( el, "rows" ) );
        text.setCols( InitUtils.getInt( el, "cols" ) );
        text.minLength = InitUtils.getInteger( el, "minLength" );
        text.maxLength = InitUtils.getInteger( el, "maxLength" );
        abstractInputHandler.fromXml( el, text );
    }

    public void populateXml( Element elThis, Text t ) {
        abstractInputHandler.populateXml( elThis, t );
        InitUtils.set(elThis, "rows", t.rows);
        InitUtils.set(elThis, "cols", t.cols);
        InitUtils.set(elThis, "minLength", t.minLength);
        InitUtils.set(elThis, "maxLength", t.maxLength);
    }

}
