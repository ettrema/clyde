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
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MvelEvaluatable.class);
    private String expr;

    public MvelEvaluatable(String s) {
        this.expr = s;
    }

    public MvelEvaluatable() {
    }

    /**
     *
     * @param rc
     * @param from - the root of the expression evaluation
     * @return
     */
    public Object evaluate(RenderContext rc, Addressable from) {
        if (log.isTraceEnabled()) {
            log.trace("evaluate: " + expr);
        }
        Templatable parent = getTemplatable(from);
        return Expression.doCalc((Templatable) from, null, expr, parent);
    }

    public Object evaluate(Object from) {
        return Expression.doCalc(from, null, expr);
    }



    public void pleaseImplementSerializable() {
    }

    private Templatable getTemplatable(Addressable from) {
        if (from instanceof Templatable) {
            return (BaseResource) from;
        } else if (from == null) {
            return null;
        } else {
            return getTemplatable(from.getContainer());
        }
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }
}
