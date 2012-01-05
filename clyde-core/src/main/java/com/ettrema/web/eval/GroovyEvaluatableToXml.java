package com.ettrema.web.eval;

import com.ettrema.web.component.Addressable;
import org.jdom.Element;
import org.jdom.Namespace;

class GroovyEvaluatableToXml implements EvaluatableToXml<GroovyEvaluatable> {

    public String getLocalName() {
        return "groovyEval";
    }

    public void populateXml(Element elEval, GroovyEvaluatable target, Namespace ns) {
        String s = null;
        if (target.getExpr() != null) {
            s = target.getExpr();
        }
        elEval.setText(s);
    }

    public GroovyEvaluatable fromXml(Element elEval, Namespace ns, Addressable container) {
        String s = elEval.getText();
        return new GroovyEvaluatable(s);
    }

    public Class getEvalClass() {
        return GroovyEvaluatable.class;
    }
}
