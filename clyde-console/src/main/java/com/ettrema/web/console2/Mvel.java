package com.ettrema.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Expression;
import com.ettrema.web.Formatter;
import com.ettrema.web.User;
import com.ettrema.console.Result;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author brad
 */
public class Mvel  extends AbstractConsoleCommand {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Eval.class);

    User theUser;

    Mvel(List<String> args, String host, String currentDir, User theUser, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
        this.theUser = theUser;
    }


    @Override
    public Result execute() {
        if (args.isEmpty()) {
            return result("missing expression");
        }
        String exp = "";
        for( String s : args ) {
            exp += s + " ";
        }

        log.debug("eval: " + exp);

        Resource r;
        try {
            r = currentResource();
        } catch (NotAuthorizedException | BadRequestException ex) {
            return result("can't lookup current resource", ex);
        }
        if (r instanceof CommonTemplated) {
            CommonTemplated t = (CommonTemplated) r;
            Map map = new HashMap();
            map.put("formatter", Formatter.getInstance());
            Object o = Expression.doCalc(t, map, exp, t.getParentFolder());
            String s = o == null ? "Expression returned null" : o.toString();
            return result(s);
        } else {
            return result("cannot eval against this type: " + r.getClass());
        }
    }
}
