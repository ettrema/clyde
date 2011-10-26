package com.ettrema.web.component;

import org.jdom.Element;

public class BooleanInput extends  AbstractInput<Boolean> {        
    
    private static final long serialVersionUID = 1L;
    
    public BooleanInput(Addressable container,String name) {
        super(container,name);
        setValue(false);
    }

    public BooleanInput(Addressable container,String name, Boolean value) {
        this(container,name);
        setValue(value);
    }
    
    public BooleanInput(Addressable container, Element el) {
        super(container,el);
    }
        
    @Override
    protected String editTemplate() {
        String checked = this.getValue() ? "checked='yes'" : "";
        return "<input type='checkbox' name='${path}' value='true' " + checked + " />";
    }

    @Override
    protected Boolean parse(String s) {
        return Boolean.parseBoolean(s);
    }    
}
