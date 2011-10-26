package com.ettrema.web.eval;

import com.ettrema.utils.GroovyUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import com.ettrema.web.component.Addressable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brad
 */
public class GroovyEvaluatable  implements Evaluatable, Serializable {

    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MvelEvaluatable.class);
    private String expr;

    public GroovyEvaluatable(String s) {
        this.expr = s;
    }

    public GroovyEvaluatable() {
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
        Map map = new HashMap();
        map.put("parent", parent);
        return GroovyUtils.exec((Templatable) from, map, expr);
    }

    public Object evaluate(Object from) {
        Map map = new HashMap();
        Object o = GroovyUtils.exec(from, map, expr);
        return o;
    }



    public void pleaseImplementSerializable() {
    }

    private Templatable getTemplatable(Addressable from) {
        if (from instanceof Templatable) {
            return (Templatable) from;
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
