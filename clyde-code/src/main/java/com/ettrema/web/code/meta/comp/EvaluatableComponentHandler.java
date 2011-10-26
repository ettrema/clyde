package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.EvaluatableComponent;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class EvaluatableComponentHandler implements ComponentHandler {

    public Class getComponentClass() {
        return EvaluatableComponent.class;
    }

    public String getAlias() {
        return "evaluatable";
    }

    public Element toXml(Component c) {
        EvaluatableComponent ec = (EvaluatableComponent) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, ec );
        return e2;

    }

    public Component fromXml(CommonTemplated res, Element el) {
        String name = el.getAttributeValue( "name" );
        if( StringUtils.isEmpty( name ) ) {
            throw new RuntimeException( "Empty component name" );
        }
        EvaluatableComponent ec = new EvaluatableComponent( res, el );
        return ec;

    }

    private void populateXml(Element e2, EvaluatableComponent ec) {
        ec._populateXml(e2);
    }

}
