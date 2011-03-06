package com.bradmcevoy.web.eval;

import org.jdom.Element;
import org.jdom.Namespace;

class ComponentReferenceToXml implements EvaluatableToXml<ComponentReference> {

    public String getLocalName() {
        return "ref";
    }

    public void populateXml(Element elEval, ComponentReference target, Namespace ns) {
        if (target.getPath() != null) {
            elEval.setText(target.getPath().toString());
        }
    }

    public ComponentReference fromXml(Element elEval, Namespace ns) {
        String s = elEval.getText();
        return new ComponentReference(s);
    }

    public Class getEvalClass() {
        return ComponentReference.class;
    }
}
