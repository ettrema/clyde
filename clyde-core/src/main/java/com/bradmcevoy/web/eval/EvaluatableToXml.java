package com.bradmcevoy.web.eval;

import org.jdom.Element;
import org.jdom.Namespace;

public interface EvaluatableToXml<T extends Evaluatable> {

    String getLocalName();

    void populateXml(Element elEval, T target, Namespace ns);

    T fromXml(Element elEval, Namespace ns);

    Class<T> getEvalClass();
}
