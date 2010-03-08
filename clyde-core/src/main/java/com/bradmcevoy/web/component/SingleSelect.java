package com.bradmcevoy.web.component;

import java.util.ArrayList;
import java.util.List;

public class SingleSelect extends AbstractInput<String> {
    
    private static final long serialVersionUID = 1L;
    
    protected List<Option> options = new ArrayList<Option>();
    
    
    public SingleSelect(Addressable container, String name) {
        super(container,name);
    }

    protected String editTemplate() {
        StringBuffer sb = new StringBuffer();
        sb.append("<select name='${path}'>");
        for( Option o : options ) {
            sb.append("<option value='").append(o.val).append("'");
            if( o.val.equals(getValue())) {
                sb.append(" selected ");
            }
            sb.append(">").append(o.text).append("</option>");
        }
        sb.append("</select>");
        return sb.toString();
    }

    protected String parse(String s) {
        return s;
    }
    
    public void addOption(String val, String text) {
        Option o = new Option();
        o.val = val;
        o.text = text;
        options.add(o);
    }
    
    public class Option {
        public String val;
        public String text;
    }
}
