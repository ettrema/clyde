
package com.bradmcevoy.web;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.WrappableComponent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;
import org.joda.time.DateTime;

public class Expression implements Component, WrappableComponent{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Expression.class);
    
    private static final long serialVersionUID = 1L;
    
    protected Addressable container;    
    protected String name;
    protected String expr;
    protected int decimals;
    protected String sDateFormat;
    protected org.joda.time.format.DateTimeFormatter dateFormat;
    
    public Expression(Addressable container, String name) {
        this.container = container;
        this.name = name;
    }

    public Expression(Addressable container, Element el) {
        this.container = container;
        fromXml(el);
    }
                
    @Override
    public void init(Addressable container) {
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return container;
    }

    @Override
    public boolean validate(RenderContext rc) {
        return true;
    }

    @Override
    public String render(RenderContext rc) {
        Templatable page = rc.getTargetPage();
        Object o = calc(page);
        if( o == null ) return "";
        String s = o.toString();
        return s;
    }

    @Override
    public String renderEdit(RenderContext rc) {
        return "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return null;
    }

    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = new Element("component");
        el.addContent(e2);
        e2.setAttribute("name",name);                
        e2.setAttribute("class",this.getClass().getName());
        InitUtils.set(e2, "decimals",decimals);
        InitUtils.setString(el, "dateformat", sDateFormat);
        e2.setText(expr);
        return e2;
    }

    public Object calc(Templatable ct) {
        Map map = new HashMap();
        return calc(ct,map);
    }
    
    public Object calc(Templatable ct, Map map) {
        log.debug("calc: " + expr + " on: " + ct.getPath() + " - " + ct.getClass());
        try {
            BaseResource targetContainer = CommonTemplated.getTargetContainer();
            map.put( "targetPage", targetContainer);
            map.put( "formatter", Formatter.getInstance());
            Object o = org.mvel.MVEL.eval(expr, ct, map);
            return o;
        } catch (Exception e) {
            throw new RuntimeException("Exception evaluating expression: " + expr + " in page: " + ct.getName(), e);
        }
    }

    private void fromXml(Element el) {
        this.name = InitUtils.getValue(el, "name");
        this.expr = el.getText();
        this.decimals = InitUtils.getInt(el,"decimals");
        this.sDateFormat = InitUtils.getValue(el, "dateformat");
        if( this.sDateFormat != null ) {
            dateFormat = org.joda.time.format.DateTimeFormat.forPattern(sDateFormat);
        }
    }

    @Override
    public void onPreProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        onPreProcess(rc, parameters, files);
    }

    @Override
    public String onProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return onProcess(rc, parameters, files);
    }

    @Override
    public String render(Addressable container, RenderContext rc) {
        CommonTemplated page = (CommonTemplated) container;
        Object o = calc(page);
        if( o == null ) return "";
        String s = getFormattedValue(o);
        return s;
    }

    @Override
    public String renderEdit(Addressable container, RenderContext rc) {
        return "";
    }

    @Override
    public boolean validate(Addressable container, RenderContext rc) {
        return validate(rc);
    }

    @Override
    public Object getValue(Addressable container) {
        if( container instanceof Templatable) {
            return calc((Templatable) container);
        } else {

            return "";
        }
    }

    @Override
    public String getFormattedValue(Addressable container) {
        Object v = getValue(container);
        return getFormattedValue(v);
    }
    
    public String getFormattedValue(Object o) {
        if( o == null ) {
            return "";
        } else if( o instanceof String ) {
            return o.toString();
        } else if( o instanceof Integer ) {
            Integer ii = (Integer) o;
            BigDecimal bd = BigDecimal.valueOf(ii.intValue());
            return bd.setScale(decimals, RoundingMode.HALF_UP).toPlainString();
        } else if( o instanceof Long ) {
            Long ll = (Long) o;
            BigDecimal bd = BigDecimal.valueOf(ll.longValue());
            return bd.setScale(decimals, RoundingMode.HALF_UP).toPlainString();
        } else if( o instanceof BigDecimal ) {
            BigDecimal bd = (BigDecimal) o;
            bd = bd.setScale(decimals, RoundingMode.HALF_UP);
            return bd.toPlainString();
        } else if( o instanceof Float ) {
            Float ff = (Float) o;
            return getFormattedFloat(ff);
        } else if( o instanceof Double ) {
            Double dd = (Double)o;
            return getFormattedFloat(dd);
        } else if( o instanceof Date ) {
            return getFormattedDate((Date)o);
        } else if( o instanceof DateTime ) {
            DateTime dt = (DateTime) o;
            return getFormattedDate(dt);
        } else if( o instanceof Boolean ) {
            return o.toString();
        } else {
            log.warn("** unhandled data type: " + o.getClass());
            return o.toString();
        }
    }

    public String getFormattedFloat(Float ff) {
        BigDecimal bd = BigDecimal.valueOf(ff.floatValue());
        bd = bd.setScale(decimals, RoundingMode.HALF_DOWN);
        return bd.toPlainString();        
    }
    
    public String getFormattedDate(Date dt) {        
        DateTime jdt = new DateTime(dt.getTime());
        return getFormattedDate(jdt);
    }

    public String getFormattedDate(DateTime jdt) {
        if(dateFormat == null ) {
            return jdt.toString();
        } else {
            return dateFormat.print(jdt);
        }
    }

    public String getFormattedFloat(Double dd) {
        BigDecimal bd = BigDecimal.valueOf((double)dd.floatValue());
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        String s = bd.toPlainString();        
        return s;
    }

    @Override
    public String toString() {
        //return getFormattedValue(container);
        return this.expr;
    }
    
    

}
