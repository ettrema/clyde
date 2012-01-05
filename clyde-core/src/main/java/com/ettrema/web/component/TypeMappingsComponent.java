
package com.ettrema.web.component;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

public class TypeMappingsComponent extends AbstractInput<List<TypeMapping>> {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TypeMappingsComponent.class);
    private static final long serialVersionUID = 1L;

    public TypeMappingsComponent(Addressable container,String name) {
        super(container,name);
        setValue(null);
    }

    public TypeMappingsComponent(Addressable container,String name, List<TypeMapping> value) {
        this(container,name);
        setValue(value);
    }
    
    public TypeMappingsComponent(Addressable container, Element el) {
        super(container,el);
    }
    

    @Override
    protected String editTemplate() {
        String template = "<input type='text' name='${path}' value='${formattedValue}' size='${input.cols}' />";
        return template;
    }

    @Override
    public String getFormattedValue() {
        List<TypeMapping> v = getValue();
        if( v == null ) return "";
        StringBuffer sb = null;
        for( TypeMapping tm : v ) {
            if( sb == null ) sb = new StringBuffer();
            else sb.append(",");
            sb.append(tm.templateName).append(":").append(tm.contentType);
        }
        return sb.toString();
    }    
    
    @Override
    protected List<TypeMapping> parse(String s) {
        List<TypeMapping> list = new ArrayList<TypeMapping>();
        for( String pair : s.split(",")) {
            String[] parts = pair.split("[:]");
            if( parts.length != 2 ) throw new IllegalArgumentException("Type mappings are not correctly formatted. Example image/jpeg:ImageTemplate,text/css:CssFile");
            TypeMapping tm = new TypeMapping(parts[0],parts[1]);
            list.add(tm);
        }
        return list;
    }
}
