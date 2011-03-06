package com.bradmcevoy.web.eval;

import org.jdom.Element;
import org.jdom.Namespace;

public class OrEvaluatableToXml implements EvaluatableToXml<OrEvaluatable> {

    public String getLocalName() {
        return "or";
    }

    public void populateXml(Element elEval, OrEvaluatable target, Namespace ns) {
        EvalUtils.setEvalDirectList(elEval, target.getExpressions(), ns);
    }

    public OrEvaluatable fromXml(Element elEval, Namespace ns) {
        OrEvaluatable and = new OrEvaluatable();
        and.setExpressions(EvalUtils.getEvalDirectList(elEval, ns));
        return and;
    }

    public Class getEvalClass() {
        return OrEvaluatable.class;
    }
}
