package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Expression;
import com.bradmcevoy.web.code.CodeMeta;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class ExpressionComponentHandler implements ComponentHandler {


    public ExpressionComponentHandler(  ) {
    }

    public Class getComponentClass() {
        return Expression.class;
    }

    public String getAlias() {
        return "expression";
    }

    public Element toXml( Component c ) {
        Expression g = (Expression) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, g );
        return e2;
    }

    public void populateXml( Element e2, Expression g ) {
        g.populateXml(e2);
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        Expression g = new Expression( res, name );
        g.fromXml(el);
        return g;
    }

}
