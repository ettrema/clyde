package com.bradmcevoy.web.eval;

import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;
import java.io.Serializable;

/**
 * A constant value
 *
 * @author brad
 */
public class ConstEvaluatable implements Evaluatable, Serializable{
    private static final long serialVersionUID = 1L;
    private Serializable value;

    public ConstEvaluatable() {
    }

    public ConstEvaluatable(Serializable value) {
        this.value = value;
    }

    @Override
    public Object evaluate(RenderContext rc, Addressable from) {
        return value;
    }

    @Override
    public Object evaluate(Object from) {
        return value;
    }



    public Object getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }

    @Override
    public void pleaseImplementSerializable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    

}
