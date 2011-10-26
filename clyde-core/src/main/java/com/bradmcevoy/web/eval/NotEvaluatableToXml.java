package com.bradmcevoy.web.eval;

import com.ettrema.utils.JDomUtils;
import com.bradmcevoy.web.component.Addressable;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;

class NotEvaluatableToXml implements EvaluatableToXml<NotEvaluatable> {

    public String getLocalName() {
        return "not";
    }

    public void populateXml(Element elEval, NotEvaluatable target, Namespace ns) {
        EvalUtils.setEvalDirect(elEval, target.getExpression(), ns);
    }

    public NotEvaluatable fromXml(Element elEval, Namespace ns, Addressable container) {
        NotEvaluatable e = new NotEvaluatable();
        List<Element> list = JDomUtils.children(elEval);
        if( list == null || list.isEmpty() ) {
            return e;
        }
        Evaluatable expr = EvalUtils.getEvalDirect(list.get(0), ns, container);
        e.setExpression(expr);
        return e;
    }

    public Class getEvalClass() {
        return NotEvaluatable.class;
    }
}
