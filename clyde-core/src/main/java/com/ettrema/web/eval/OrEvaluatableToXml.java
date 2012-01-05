package com.ettrema.web.eval;

import com.ettrema.web.component.Addressable;
import org.jdom.Element;
import org.jdom.Namespace;

public class OrEvaluatableToXml implements EvaluatableToXml<OrEvaluatable> {

    public String getLocalName() {
        return "or";
    }

    public void populateXml(Element elEval, OrEvaluatable target, Namespace ns) {
        EvalUtils.setEvalDirectList(elEval, target.getExpressions(), ns);
    }

    public OrEvaluatable fromXml(Element elEval, Namespace ns, Addressable container) {
        OrEvaluatable and = new OrEvaluatable();
        and.setExpressions(EvalUtils.getEvalDirectList(elEval, ns, container));
        return and;
    }

    public Class getEvalClass() {
        return OrEvaluatable.class;
    }
}
