package com.ettrema.web.query;

import java.util.Set;

/**
 *
 * @author brad
 */
public interface FieldSource {
    /**
     * Get the value of the named field.
     *
     * @param name
     * @return
     */
    Object get(String name);

    /**
     * Get the object which is used as the context for evaluating expressions.
     * This will be the physical row this FieldSource wraps, or a list of rows
     * in an aggregating query
     *
     * @return
     */
    Object getData();

    Set<String> getKeys();

}
