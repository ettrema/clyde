package com.ettrema.web.csv;

import java.io.Serializable;

/**
 *
 * @author brad
 */
public class FieldAndName implements Serializable{

    private static final long serialVersionUID = 1L;

    private String name;
    private String expr;

    public FieldAndName( String name, String expr ) {
        this.name = name;
        this.expr = expr;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return the expr
     */
    public String getExpr() {
        return expr;
    }

    /**
     * @param expr the expr to set
     */
    public void setExpr( String expr ) {
        this.expr = expr;
    }


}
