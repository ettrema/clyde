package com.bradmcevoy.web.component;

import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jdom.Element;

public class NumberDef extends TextDef {
    
    private static final long serialVersionUID = 1L;

    protected int decimals;
    
    public NumberDef(Addressable container,String name) {
        super(container,name);
    }
    
    public NumberDef(Addressable container, Element el) {
        super(container, el);
        this.decimals = InitUtils.getInt( el, "decimals");
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = super.toXml( container, el );
        InitUtils.set( e2, "decimals", decimals);
        return e2;
    }
    @Override
    public Object parseValue(ComponentValue cv, Templatable ct,String s) {
        if( s == null ) return null;
        s = s.trim();
        if( s.length() == 0 ) return null;
        return Double.parseDouble(s);
    }

    @Override
    public String formatValue( Object v ) {
        if (v == null) {
            return "";
        } else if( v instanceof Double ) {
            Double dd = (Double) v;
            BigDecimal bd = new BigDecimal( dd );
            bd = bd.setScale( decimals, RoundingMode.HALF_UP);
            return bd.toPlainString();
        } else {
            return v.toString();
        }
    }



    public int getDecimals() {
        return decimals;
    }

    public void setDecimals( int decimals ) {
        this.decimals = decimals;
    }
    
    @Override
    public String render(ComponentValue c, RenderContext rc) {
        return formatValue( c.getValue());
    }
    
}
