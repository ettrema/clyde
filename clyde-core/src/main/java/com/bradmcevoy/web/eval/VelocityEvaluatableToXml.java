package com.bradmcevoy.web.eval;

import com.bradmcevoy.utils.JDomUtils;
import org.jdom.Element;
import org.jdom.Namespace;

class VelocityEvaluatableToXml implements EvaluatableToXml<VelocityEvaluatable> {

    public String getLocalName() {
        return "velocity";
    }

    public void populateXml(Element elEval, VelocityEvaluatable target, Namespace ns) {
        String s = null;
        if (target.getTemplate() != null) {
            s = target.getTemplate();
        }
        JDomUtils.setInnerXml(elEval, s);
    }

    public VelocityEvaluatable fromXml(Element elEval, Namespace ns) {
        String s = JDomUtils.getInnerXml(elEval);
        return new VelocityEvaluatable(s);
    }

    public Class getEvalClass() {
        return VelocityEvaluatable.class;
    }
}
