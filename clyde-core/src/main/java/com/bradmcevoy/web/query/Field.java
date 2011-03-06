package com.bradmcevoy.web.query;

import com.bradmcevoy.web.eval.Evaluatable;
import java.io.Serializable;

/**
 * A field is a name and optionally an expression of some kind.
 *
 * If evaluatable name must identify a field in the source
 *
 * @author brad
 */
public class Field implements Serializable{
    private static final long serialVersionUID = 1L;
    private String name;
    private Evaluatable evaluatable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Evaluatable getEvaluatable() {
        return evaluatable;
    }

    public void setEvaluatable(Evaluatable evaluatable) {
        this.evaluatable = evaluatable;
    }

    
}
