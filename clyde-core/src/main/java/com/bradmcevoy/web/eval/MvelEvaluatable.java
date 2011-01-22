package com.bradmcevoy.web.eval;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Expression;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.Addressable;
import java.io.Serializable;

/**
 *
 * @author brad
 */
public class MvelEvaluatable implements Evaluatable, Serializable {

    private static final long serialVersionUID = 1L;
    private String expr;

    public MvelEvaluatable(String s) {
        this.expr = s;
    }

    public MvelEvaluatable() {
    }



    public Object evaluate(RenderContext rc, Addressable from) {
        BaseResource parent = getPhysicalParent(from.getContainer());
        return Expression.doCalc((Templatable) from, null, expr, parent);
    }

    public void pleaseImplementSerializable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private BaseResource getPhysicalParent(Addressable from) {
        if (from instanceof BaseResource) {
            return (BaseResource) from;
        } else if (from == null) {
            return null;
        } else {
            return getPhysicalParent(from.getContainer());
        }
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    
}
