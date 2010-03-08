package com.bradmcevoy.web.component;

import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.velocity.VelocityInterpreter;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

public class HtmlInput extends Text {
    
    private static final long serialVersionUID = 1L;
    
    public HtmlInput(Addressable container,String name) {
        super(container,name);
    }
    
    public HtmlInput(Addressable container,String name, String value) {
        this(container,name);
        setValue(value);
    }

    public HtmlInput(Addressable container, Element el) {
        super(container,el);
    }
    
    @Override
    protected String editTemplate() {
        String template;
        template = "<textarea name='${path}' rows='${input.rows}' cols='${input.cols}'>${formattedValue}</textarea>";
        return template;
    }

    @Override
    public String render(RenderContext rc) {
        String template = getValue();
        // String s = TemplateInterpreter.evalToString(template,rc);
        VelocityContext vc = new VelocityContext();
        vc.put("rc", rc);
        String s = VelocityInterpreter.evalToString(template, vc);
        return s;
    }
    


}
