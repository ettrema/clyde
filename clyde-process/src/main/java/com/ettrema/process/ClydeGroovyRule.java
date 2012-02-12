package com.ettrema.process;

import com.bradmcevoy.process.AbstractRule;
import com.bradmcevoy.process.ProcessContext;
import com.bradmcevoy.process.SetVariable;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.Serializable;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;
import com.ettrema.web.security.CurrentUserService;

/**
 *
 * @author brad
 */
public class ClydeGroovyRule extends AbstractRule implements Serializable {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetVariable.class);
    private static final long serialVersionUID = 1L;
    public String expr;

    public ClydeGroovyRule() {
    }

    public ClydeGroovyRule(Element el) {
        expr = el.getText();
    }

    public ClydeGroovyRule(String varName, String value) {
        this.expr = value;
    }

    @Override
    public void populateXml(Element el) {
        el.setText(expr);
    }

    @Override
    public void arm(ProcessContext context) {
        
    }

    @Override
    public void disarm(ProcessContext context) {
        
    }

    @Override
    public boolean eval(ProcessContext context) {
        try {            
            Binding binding = new Binding();
            binding.setVariable("context", context);
            binding.setVariable("securityContextUser", _(CurrentUserService.class).getSecurityContextUser());            
            binding.setVariable("user", _(CurrentUserService.class).getOnBehalfOf());            
            binding.setVariable("token", context.getToken());
            binding.setVariable("process", context.getProcess() );
            binding.setVariable("attributes", context.getAttributes() );
            GroovyShell shell = new GroovyShell(binding);
            Object result = shell.evaluate(expr);
            return toBool(result);
        } catch (Throwable e) {
            throw new RuntimeException("Exception evaluating Groovy script: " + expr, e);
        }
    }
}
