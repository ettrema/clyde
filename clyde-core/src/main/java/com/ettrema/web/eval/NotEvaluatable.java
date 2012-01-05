package com.ettrema.web.eval;

import com.ettrema.web.Formatter;
import com.ettrema.web.RenderContext;
import com.ettrema.web.component.Addressable;
import java.io.Serializable;

/**
 *
 * @author brad
 */
public class NotEvaluatable implements Evaluatable, Serializable{

    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MvelEvaluatable.class);
    private Evaluatable expression;

    public Object evaluate(RenderContext rc, Addressable from) {
        Object n = expression.evaluate(rc, from);
        Boolean b = Formatter.getInstance().toBool(n);
        if( b != null ) {
            return !b.booleanValue();
        } else {
            return null;
        }
    }

    public Object evaluate(Object from) {
        Object n = expression.evaluate(from);
        Boolean b = Formatter.getInstance().toBool(n);
        if( b != null ) {
            return !b.booleanValue();
        } else {
            return null;
        }
    }

    public void pleaseImplementSerializable() {
    }

    public Evaluatable getExpression() {
        return expression;
    }

    public void setExpression(Evaluatable expression) {
        this.expression = expression;
    }

    

}
