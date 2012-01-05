
package com.ettrema.process;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.process.ActionHandler;
import com.bradmcevoy.process.ProcessContext;
import com.bradmcevoy.process.SetVariable;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Component;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.ComponentUtils;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.Text;
import java.io.Serializable;
import org.jdom.Element;

public class SetHtml implements ActionHandler, Serializable{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetVariable.class);
    
    private static final long serialVersionUID = 1L;
    
    public String path;

    private String html;
    
    public SetHtml(Element el) {
        path = el.getAttributeValue("path");
        html = InitUtils.getElementValue( el, "html");
    }
    
    @Override
    public void populateXml(Element el) {
        el.setAttribute("path", path);
        InitUtils.setElementString( el, "html", html);
    }

    @Override
    public void process(ProcessContext processContext) {
        BaseResource res = (BaseResource) processContext.getAttribute("res");
        Component c = ComponentUtils.findComponent(Path.path(path), res);
        if( c == null ) {
            throw new IllegalArgumentException("Couldnt find component: " + path + " from resource: " + res.getHref());
        }
        Addressable resToSave;
        if( c instanceof ComponentValue ) {
            ComponentValue cv = (ComponentValue) c;
            cv.setValue(html);
            resToSave = cv.getContainer();
        } else if( c instanceof Text ) {
            Text t = (Text) c;
            t.setValue(html);
            resToSave = t.getContainer();
        } else {
            throw new IllegalArgumentException("Unsupported component type for setting. Try a componentvalue or a text component: " + c.getClass());
        }
        
        if( resToSave instanceof BaseResource ) {
            BaseResource r = (BaseResource) resToSave;
            r.save();
        }        
    }

}
