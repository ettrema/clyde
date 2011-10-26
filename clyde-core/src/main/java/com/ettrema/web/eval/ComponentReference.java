package com.ettrema.web.eval;

import com.bradmcevoy.common.Path;
import com.ettrema.web.Component;
import com.ettrema.web.Expression;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.ComponentUtils;
import java.io.Serializable;

/**
 *
 * @author brad
 */
public class ComponentReference implements Evaluatable, Serializable{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ComponentReference.class);
    private static final long serialVersionUID = 1L;

    private Path path;

    public ComponentReference(String s) {
        this.path = Path.path(s);
    }

    public ComponentReference() {
    }

    public Object evaluate(RenderContext rc, Addressable from) {
        Component target = ComponentUtils.findComponent( path, (Templatable) from);
        if( target == null ) {
            log.trace("target not found: " + path + " from: " + from.getPath());
            return null;
        } else {
            if( target instanceof Expression) {
                Expression expr = (Expression) target;
                return expr.calc((Templatable) from);
            } else {
                return target.render(rc);
            }
        }
    }

    public Object evaluate(Object from) {
        Component target = ComponentUtils.findComponent( path, (Templatable) from);
        if( target == null ) {
            return null;
        } else {
            if( target instanceof Expression) {
                Expression expr = (Expression) target;
                return expr.calc((Templatable) from);
            } else {
                return target;
            }
        }
    }



    public void pleaseImplementSerializable() {
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }



}
