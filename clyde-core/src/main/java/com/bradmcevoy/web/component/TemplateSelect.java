package com.bradmcevoy.web.component;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.SystemComponent;
import com.bradmcevoy.web.Template;
import java.util.List;
import org.jdom.Element;

public class TemplateSelect extends AbstractInput<String> implements SystemComponent {
    
    private static final long serialVersionUID = 1L;
    
    public TemplateSelect(Addressable container, String name) {
        super(container,name);
    }

    public TemplateSelect(Addressable container, Element el) {
        super(container,el);
    }
    
    @Override
    protected String editTemplate() {
        if( !(container instanceof BaseResource) ) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<select name='${path}'>");
        BaseResource target = (BaseResource) container;
        List<Template> arr = target.getParent().getAllowedTemplates();
        if( arr != null ) {
            for( Template p : arr ) {  // TODO: configurable source
                sb.append("<option value='").append(p.getName()).append("'");
                if( p.getName().equals(getValue())) {
                    sb.append(" selected ");
                }
                sb.append(">").append(p.getName()).append("</option>");
            }
        }
        sb.append("</select>");
        return sb.toString();
    }

    @Override
    protected String parse(String s) {
        return s;
    }
}
