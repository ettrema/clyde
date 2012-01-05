package com.ettrema.web.eval;

import com.ettrema.web.component.Addressable;
import org.jdom.Element;
import org.jdom.Namespace;

class ConstEvaluatableToXml implements EvaluatableToXml<ConstEvaluatable> {

    public String getLocalName() {
        return "const";
    }

    public void populateXml(Element elEval, ConstEvaluatable target, Namespace ns) {
        String s = null;
        if (target.getValue() != null) {
            s = target.getValue().toString();
        }
        elEval.setText(s);
    }

    public ConstEvaluatable fromXml(Element elEval, Namespace ns, Addressable container) {
        String s = elEval.getText();
        return new ConstEvaluatable(s);
    }

    public Class getEvalClass() {
        return ConstEvaluatable.class;
    }
}
