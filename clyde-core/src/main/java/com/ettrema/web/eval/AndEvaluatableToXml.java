package com.ettrema.web.eval;

import com.ettrema.web.component.Addressable;
import org.jdom.Element;
import org.jdom.Namespace;

class AndEvaluatableToXml implements EvaluatableToXml<AndEvaluatable> {

    public String getLocalName() {
        return "and";
    }

    public void populateXml(Element elEval, AndEvaluatable target, Namespace ns) {
        EvalUtils.setEvalDirectList(elEval, target.getExpressions(), ns);
    }

    public AndEvaluatable fromXml(Element elEval, Namespace ns, Addressable container) {
        AndEvaluatable and = new AndEvaluatable();
        and.setExpressions(EvalUtils.getEvalDirectList(elEval, ns, container));
        return and;
    }

    public Class getEvalClass() {
        return AndEvaluatable.class;
    }
}
