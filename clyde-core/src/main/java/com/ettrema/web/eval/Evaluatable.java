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
    /**
     * Evaluate given a templating resource as the source
     *
     * This is intended to cover the common case where templated resources have
     * two important contextual hierarchies - that of the located resource, and that
     * of the template resource which implied it.
     *
     * Eg a template might define a subpage (eg /templates/event/details), and a request might locate a manifestation
     * of that subpage under some physical resource (eg /calendar/agm/details).
     *
     * Both of those contexts are passed in to this method
     *
     * @param rc
     * @param from
     * @return
     */
    Object evaluate(RenderContext rc, Addressable from);

    /**
     * This caters for the case where an evaluation is executed against some
     * object which is not a templated resource. Eg it might be a result
     *  in an expression of query.
     *
     * @param source
     * @return
     */
    Object evaluate(Object from);

    /**
     * This is so developers remember to implement serializable on all implementations
     * , and to define a serial version id
     */
    void pleaseImplementSerializable();
}
