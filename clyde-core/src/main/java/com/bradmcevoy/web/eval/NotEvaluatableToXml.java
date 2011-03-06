package com.bradmcevoy.web.eval;

import org.jdom.Element;
import org.jdom.Namespace;

class NotEvaluatableToXml implements EvaluatableToXml<NotEvaluatable> {

    public String getLocalName() {
        return "not";
    }

    public void populateXml(Element elEval, NotEvaluatable target, Namespace ns) {
        target.getExpression();
        EvalUtils.setEvalDirect(elEval, target, ns);
    }

    public NotEvaluatable fromXml(Element elEval, Namespace ns) {
        NotEvaluatable e = new NotEvaluatable();
        Evaluatable expr = EvalUtils.getEvalDirect(elEval, ns);
        e.setExpression(expr);
        return e;
    }

    public Class getEvalClass() {
        return NotEvaluatable.class;
    }
}
