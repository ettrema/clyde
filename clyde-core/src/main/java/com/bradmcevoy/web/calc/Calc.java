package com.bradmcevoy.web.calc;

import com.bradmcevoy.web.BaseResourceList;
import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.ValueHolder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 *
 */
public class Calc {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Calc.class);

    List<Templatable> list;

    public Calc(List<Templatable> list) {
        this.list = list;
    }

    public Object eval(String mvelExpr, Object r) {
        Object o = org.mvel.MVEL.eval(mvelExpr, r);
        //log.debug( "eval: returned: " + o);
        if( o instanceof ComponentValue ) {
            ComponentValue cv = (ComponentValue) o;
            return cv.getValue();
        } else {
            return o;
        }
    }


    public BigDecimal sum(String mvelExpr) {
        return sum(mvelExpr,0);
    }

    public BaseResourceList filter(String mvelExpr) {
        //log.debug( "filter");
        ListFilter filter = new ListFilter();
        accumulate(filter, mvelExpr);
        return filter.dest;
    }

    public BigDecimal sum(String mvelExpr, int decimals) {
//        log.debug("sum: " + mvelExpr);
        Sumor summer = new Sumor(decimals);
        accumulate(summer, mvelExpr);
        return summer.value;
    }

    void accumulate(Accumulator a, String mvelExpr) {
        for( Templatable r : list) {
            Object o = eval(mvelExpr, r);
            a.accumulate(r, o);
        }
    }

    public static BigDecimal toBigDecimal(Object o, int decimals) {
        if( o instanceof ValueHolder) {
            ValueHolder c = (ValueHolder)o;
            o = c.getValue();
        }
        if( o instanceof Integer) {
            Integer ii = (Integer) o;
            return new BigDecimal(ii.intValue());
        } else if( o instanceof Double ) {
            Double dd = (Double)o;
            return new BigDecimal(dd.doubleValue()).setScale(decimals, RoundingMode.HALF_UP);
        } else if( o instanceof Float) {
            Float ff = (Float) o;
            return new BigDecimal(ff);
        } else if( o instanceof String ) {
            Double dd = Formatter.getInstance().toDouble(o);
            return toBigDecimal(dd, decimals);
        } else {
            log.warn("unhandled type: " + o.getClass());
            return null;
        }
    }
}
