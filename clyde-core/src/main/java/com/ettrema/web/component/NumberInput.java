package com.ettrema.web.component;

import com.bradmcevoy.utils.IntegerUtils;
import org.jdom.Element;

public class NumberInput extends AbstractInput<Integer>{
    
    private static final long serialVersionUID = 1L;
    
    public Integer cols;
    
    public NumberInput(Addressable container,String name) {
        super(container,name);
    }

    public NumberInput(Addressable container, Element el) {
        super(container,el);
    }
    
    @Override
    protected String editTemplate() {
        return "<input type='${input.type}' name='${path}' id='${path}' value='${formattedValue}' size='${input.cols}' />";
    }    

    @Override
    protected Integer parse(String s) {
        if( s == null ) return null;
        s = s.trim();
        if( s.length() == 0 ) return null;
        return Integer.parseInt(s);
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
