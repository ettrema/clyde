package com.bradmcevoy.web.eval;

import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;

/**
 * For objects which can be evaluated
 *
 * This is intended to be used as a general mechanism for paramaterising components
 *
 * Eg instead of an email component having a subject expression as a property,
 * it should have an eval object. This might be a reference to another component, a
 * static value, an expression or a template script.
 *
 * @author brad
 */
public interface Evaluatable {
    Object evaluate(RenderContext rc, Addressable from);

    void pleaseImplementSerializable();
}
