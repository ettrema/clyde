package com.ettrema.web.eval;

import com.ettrema.web.IUser;
import com.ettrema.web.RenderContext;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.CommonComponent;
import com.ettrema.web.security.CurrentUserService;
import com.ettrema.web.velocity.VelocityInterpreter;
import java.io.Serializable;
import org.apache.velocity.VelocityContext;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class VelocityEvaluatable implements Evaluatable, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VelocityEvaluatable.class);
    private static final long serialVersionUID = 1L;
    private String template;

    public VelocityEvaluatable(String s) {
        this.template = s;
    }

    public VelocityEvaluatable() {
    }



	@Override
    public Object evaluate(RenderContext rc, Addressable from) {
        IUser user = _(CurrentUserService.class).getOnBehalfOf();
        VelocityContext vc = CommonComponent.velocityContext(rc, null, from.getPath(), user);
        return _render(template, vc);
    }

	@Override
    public Object evaluate(Object from) {
        IUser user = _(CurrentUserService.class).getOnBehalfOf();
        VelocityContext vc = CommonComponent.velocityContext(null, from, null, user);
        return _render(template, vc);
    }



    private String _render(String template, VelocityContext vc) {
        try {
            return VelocityInterpreter.evalToString(template, vc);
        } catch (Throwable e) {
            throw new RuntimeException("Exception rendering template: " + template, e);
        }
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    

    public void pleaseImplementSerializable() {
    }
}
