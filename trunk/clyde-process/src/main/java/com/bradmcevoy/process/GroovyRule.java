package com.bradmcevoy.process;

import com.bradmcevoy.web.Formatter;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.Serializable;
import org.jdom.Element;


/**
 * Just runs a Groovy script. Doesnt do anything with the result
 *
 *
 * @author brad
 */
public class GroovyRule extends AbstractRule implements Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetVariable.class);
    private static final long serialVersionUID = 1L;
    public String expr;

    public GroovyRule() {
    }

    public GroovyRule(Element el) {
        expr = el.getText();
    }

    public GroovyRule(String varName, String value) {
        this.expr = value;
    }

    @Override
    public void populateXml(Element el) {
        el.setText(expr);
    }

    @Override
    public boolean eval(ProcessContext context) {
        try {
            Formatter formatter = Formatter.getInstance();
            Binding binding = new Binding();
            binding.setVariable("context", context);
            binding.setVariable("formatter", formatter);
            binding.setVariable("token", context.getToken());
            binding.setVariable("process", context.getProcess());
            binding.setVariable("attributes", context.getAttributes());
            GroovyShell shell = new GroovyShell(binding);
            Object o = shell.evaluate(expr);
            boolean result = toBool(o);
            if (log.isTraceEnabled()) {
                log.trace("got result: " + result);
            }
            return result;
        } catch (Throwable e) {
            throw new RuntimeException("Exception evaluating Groovy script: " + expr, e);
        }
    }
}
