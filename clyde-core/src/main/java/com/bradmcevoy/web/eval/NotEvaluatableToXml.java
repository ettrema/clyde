package com.bradmcevoy.web.eval;

import com.bradmcevoy.web.component.Addressable;
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

    public NotEvaluatable fromXml(Element elEval, Namespace ns, Addressable container) {
        NotEvaluatable e = new NotEvaluatable();
        Evaluatable expr = EvalUtils.getEvalDirect(elEval, ns, container);
        e.setExpression(expr);
        return e;
    }

    public Class getEvalClass() {
        return NotEvaluatable.class;
    }
}
