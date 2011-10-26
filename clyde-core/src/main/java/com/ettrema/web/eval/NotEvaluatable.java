package com.bradmcevoy.web.eval;

import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;
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
