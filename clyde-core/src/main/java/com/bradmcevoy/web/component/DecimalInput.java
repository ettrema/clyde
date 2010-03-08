
package com.bradmcevoy.web.component;

import com.bradmcevoy.utils.IntegerUtils;
import java.math.BigDecimal;
import org.jdom.Element;

public class DecimalInput extends AbstractInput<BigDecimal>{
    
    private static final long serialVersionUID = 1L;
    
    public Integer cols;
    
    public DecimalInput(Addressable container,String name) {
        super(container,name);
    }

    public DecimalInput(Addressable container, Element el) {
        super(container,el);
    }
    
    @Override
    protected String editTemplate() {
        return "<input type='text' name='${path}' value='${formattedValue}' size='${input.cols}' />";
    }    

    @Override
    protected BigDecimal parse(String s) {
        if( s == null ) return null;
        s = s.trim();
        if( s.length() == 0 ) return null;
        double d = Double.parseDouble(s);
        return BigDecimal.valueOf(d);
    }
    
    @Override
    public void fromXml(Element el) {
        super.fromXml(el);
        cols = IntegerUtils.parseInteger( el.getAttributeValue("cols") );
    }

    @Override
    public Element toXml(Addressable container,Element el) {
        Element elThis = super.toXml(container,el);
        elThis.setAttribute("cols",cols==null?"":cols.toString());
        return elThis;
    }        
}



