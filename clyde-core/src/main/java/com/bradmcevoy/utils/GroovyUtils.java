
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
    public static Object exec(Templatable aThis, Map map, String script) {
        Binding binding = new Binding();
        IUser user = _(CurrentUserService.class).getOnBehalfOf();
        binding.setVariable("targetPage", aThis);
        binding.setVariable("user", user);
        binding.setVariable("formatter", Formatter.getInstance());
        GroovyShell shell = new GroovyShell(binding);

        return shell.evaluate(script);
    }
}
