
package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.web.User;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class EvalFactory extends AbstractFactory{

    public EvalFactory() {
        super( "Evaluate an expression. The expression is in velocity syntax, normally start with $targetPage", new String[]{"eval"} );
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Eval(args,host,currentDir,(User) auth.getTag(),resourceFactory); 
    }
    
}
