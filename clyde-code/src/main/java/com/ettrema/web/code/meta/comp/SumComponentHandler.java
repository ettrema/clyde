package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Sum;
import com.bradmcevoy.web.code.CodeMeta;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class SumComponentHandler implements ComponentHandler {

    private final ExpressionComponentHandler expressionComponentHandler;

    public SumComponentHandler( ExpressionComponentHandler expressionComponentHandler ) {
        this.expressionComponentHandler = expressionComponentHandler;
    }

    public Class getComponentClass() {
        return Sum.class;
    }

    public String getAlias() {
        return "sum";
    }

    public Element toXml( Component c ) {
        Sum g = (Sum) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, g );
        return e2;
    }

    private void populateXml( Element e2, Sum g ) {
        expressionComponentHandler.populateXml( e2, g );
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        Sum g = new Sum( res, name );
        g.fromXml(el);
        return g;
    }

}
