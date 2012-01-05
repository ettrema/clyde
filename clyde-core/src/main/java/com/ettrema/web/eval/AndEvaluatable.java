package com.ettrema.web.eval;

import com.ettrema.web.Formatter;
import com.ettrema.web.RenderContext;
import com.ettrema.web.component.Addressable;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author brad
 */
public class AndEvaluatable implements Evaluatable, Serializable{

    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MvelEvaluatable.class);
    private List<Evaluatable> expressions;

    public Object evaluate(RenderContext rc, Addressable from) {
        for( Evaluatable ev : expressions) {
            Object n = ev.evaluate(rc, from);
            Boolean b = Formatter.getInstance().toBool(n);
            if( b == null || !b.booleanValue()) {
                return Boolean.FALSE;
            }
        }
        return true;
    }

    public Object evaluate(Object from) {
        for( Evaluatable ev : expressions) {
            Object n = ev.evaluate(from);
            Boolean b = Formatter.getInstance().toBool(n);
            if( b == null || !b.booleanValue()) {
                return Boolean.FALSE;
            }
        }
        return true;
    }

    public void pleaseImplementSerializable() {
    }

    public List<Evaluatable> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Evaluatable> expressions) {
        this.expressions = expressions;
    }

    

}
