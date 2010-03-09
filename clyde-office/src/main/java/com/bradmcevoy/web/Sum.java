
package com.bradmcevoy.web;

import com.bradmcevoy.web.component.Addressable;
import java.math.BigDecimal;
import org.jdom.Element;

public class Sum extends Expression {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Sum.class);
    
    private static final long serialVersionUID = 1L;
        
    public Sum(Addressable container, String name) {
        super(container, name);
    }

    public Sum(Addressable container, Element el) {
        super(container, el);
    }

    public BigDecimal getValue(Templatable page) {
        BigDecimal bd = BigDecimal.valueOf(0);
        Folder folder = findFolder(page);
        for( Templatable ct : folder.getChildren() ) {
            BigDecimal val = calc(ct);
            if( val != null )  bd = bd.add(val);
        }
        return bd;
    }
    
    protected Folder findFolder(Templatable child) {
        if( child instanceof Folder ) return (Folder) child;
        return findFolder(child.getParent());
    }
    
    
    
    @Override
    public String render(RenderContext rc) {
        Templatable page = rc.getTargetPage();
//        return getFormattedValue(page);
        BigDecimal bd = getValue(page); 
        if( bd == null ) return "";
        return getFormattedValue(bd);
        //return bd.toPlainString();
    }

    @Override
    public String render(Addressable container, RenderContext rc) {
        log.debug("render");
        CommonTemplated page = (CommonTemplated) container;
        return getFormattedValue(page);
//        BigDecimal bd = getValue(page);
//        if( bd == null ) return "";
//        return bd.toPlainString();
    }    

    @Override
    public BigDecimal calc(Templatable ct) {
        log.debug("calculating: " + expr + " on: " + ct.getPath() + " - " + ct.getClass());        
        Object o = org.mvel.MVEL.eval(expr, ct);
        if( o == null ) return null;
        if( o instanceof BigDecimal ) return (BigDecimal) o;
        if( o instanceof Double ) {
            Double d = (Double) o;
            return BigDecimal.valueOf(d.doubleValue());
        }
        if( o instanceof Float ) {
            Float f = (Float) o;
            return BigDecimal.valueOf(f.doubleValue());
        }
        if( o instanceof Integer ) {
            Integer i = (Integer) o;
            return BigDecimal.valueOf(i.intValue());
        }
        if( o instanceof Long ) {
            Long l = (Long) o;
            return BigDecimal.valueOf(l.longValue());
        }
        throw new RuntimeException("Unhandled data type: " + o.getClass() + ", value: " + o + " in file: " + ct.getName());
    }
    
}
