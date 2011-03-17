package com.bradmcevoy.utils;

import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.security.CurrentUserService;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.Map;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class GroovyUtils {

    public static Object exec(Templatable res, Map map, String script) {
        Binding binding = new Binding();
        IUser user = _(CurrentUserService.class).getOnBehalfOf();
        binding.setVariable("targetPage", res);
        binding.setVariable("user", user);
        binding.setVariable("formatter", Formatter.getInstance());
        GroovyShell shell = new GroovyShell(binding);
        try {
            return shell.evaluate(script);
        } catch (Throwable e) {
            throw new RuntimeException("Exception in groovy script: " + script + " in resource: " + res.getHref(), e);
        }
    }

    public static Object exec(Object data, Map map, String script) {
        Binding binding = new Binding();
        IUser user = _(CurrentUserService.class).getOnBehalfOf();
        binding.setVariable("data", data);
        binding.setVariable("user", user);
        binding.setVariable("formatter", Formatter.getInstance());
        GroovyShell shell = new GroovyShell(binding);
        try {
            return shell.evaluate(script);
        } catch (Throwable e) {
            throw new RuntimeException("Exception in groovy script: " + script , e);
        }
    }
}
