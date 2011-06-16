package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Expression;
import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.User;
import com.ettrema.console.Result;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Resource r = currentResource();
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
