package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.velocity.VelocityInterpreter;
import java.io.Serializable;
import org.apache.velocity.VelocityContext;

public abstract class CommonComponent implements Component, Serializable {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommonComponent.class);
    private static final long serialVersionUID = 1L;

    public static Path getPath(Component c, RenderContext rc) {
        // note: changed to name only so that new pages works. might need to re-enable
        // full path, but this should be conditional on whether or not the component is a child of the target page
        String nm = c.getName();
        return Path.path(nm);
//        Path p = rc.page.getPath();
//        p = p.child(name.getValue());
//        return p;
        
    }
    
    public Path getPath(RenderContext rc) {
        return CommonComponent.getPath(this, rc);
    }

    protected VelocityContext velocityContext(RenderContext rc, Object value) {
        VelocityContext vc = new VelocityContext();
        vc.put("rc", rc);
        vc.put("path", this.getPath(rc));
        if (value == null) {
            value = "";
        }
        if( rc != null ) {
            Templatable page = rc.page;
            if( page != null ) {
                vc.put("page", page);
            }
            Templatable targetPage = rc.getTargetPage();
            if( targetPage != null ){
                vc.put("targetPage", targetPage);
                vc.put("folder", targetPage.getParent());
                vc.put("web", targetPage.getWeb());
            }
        }
        RequestParams rq = RequestParams.current();
        if( rq != null ) {
            vc.put("auth", rq.getAuth());
        }
        vc.put( "formatter", Formatter.getInstance());
        vc.put("value", value);
        vc.put("def", this);
        vc.put("input", this);
        RequestParams requestParams = RequestParams.current();
        vc.put("request", requestParams);
        return vc;
    }

    protected String _render(String template, VelocityContext vc) {
        try{
            return VelocityInterpreter.evalToString(template, vc);
        } catch (Throwable e) {
            log.error("Exception rendering template: " + template, e);
            return "ERR";            
        }
    }
}
