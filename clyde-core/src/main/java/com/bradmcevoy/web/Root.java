
package com.bradmcevoy.web;

import com.bradmcevoy.web.component.DeleteCommand;
import com.bradmcevoy.web.component.HtmlDef;
import com.bradmcevoy.web.component.HtmlInput;
import com.bradmcevoy.web.component.SaveCommand;
import java.util.HashMap;
import java.util.Map;

public class Root extends Template {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Root.class);
    public static final String HTML_TEMPLATE = "$rc.doBody()";
    
    private static final long serialVersionUID = 1L;
    
    private static final Map<Folder,Root> cache = new HashMap<Folder,Root>();
    
    public static synchronized Root getInstance(Folder templates) {
        Root r = cache.get(templates);
        if( r == null ) {
            r = new Root(templates);
            cache.put(templates, r);
        }
        return r;
    }
    
    private Root(Folder templates) {
        super(templates, "root");
              
        HtmlInput root = new HtmlInput(this, "root");
        root.cols = 80;
        root.rows = 30;
        root.setValue(HTML_TEMPLATE);
        this.getComponents().add(root);

        Component c;
        c = new SaveCommand(this, "save");
        this.getComponents().add(c);
        c = new DeleteCommand(this, "delete");
        this.getComponents().add(c);
        
        HtmlDef body = new HtmlDef(this, "body");
        this.getComponentDefs().add(body);
    }
}
