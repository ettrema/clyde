package com.ettrema.web.eval;

import com.ettrema.web.component.Addressable;
import org.jdom.Element;
import org.jdom.Namespace;

class MvelEvaluatableToXml implements EvaluatableToXml<MvelEvaluatable> {

    public String getLocalName() {
        return "mvel";
    }

    public void populateXml(Element elEval, MvelEvaluatable target, Namespace ns) {
        String s = null;
        if (target.getExpr() != null) {
            s = target.getExpr();
        }
        elEval.setText(s);
    }

    public MvelEvaluatable fromXml(Element elEval, Namespace ns, Addressable container) {
        String s = elEval.getText();
        return new MvelEvaluatable(s);
    }

    public Class getEvalClass() {
        return MvelEvaluatable.class;
    }
}
