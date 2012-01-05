package com.ettrema.web.component;

import com.ettrema.web.RenderContext;
import org.mvel.TemplateInterpreter;
import org.jdom.Element;

/**
 * A text component which renders its output using MVEL
 *
 * @author brad
 */
public class TemplateInput extends Text {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TemplateInput.class);
    
    private static final long serialVersionUID = 1L;
    
    public TemplateInput(Addressable container,String name) {
        super(container,name);
        setValue("");
    }

    public TemplateInput(Addressable container, Element el) {
        super(container,el);
    }
    
    @Override
    public String render(RenderContext rc) {
        // TODO: security isnt using this. need to refactor
        String t = this.getValue();
        if( t == null || t.length() == 0 ) {
            return "";
        }
        try {
            String s = TemplateInterpreter.evalToString(t,new TemplateSource(rc,this.getPath(),this.getValue(),this));
            return s;
        } catch(Throwable e) {
            log.error("Exception rendering template: " + t,e);
            return "ERR";
        }
    }   
    
}
