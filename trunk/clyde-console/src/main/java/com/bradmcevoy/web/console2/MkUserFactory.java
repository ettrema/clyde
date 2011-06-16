package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class MkUserFactory extends AbstractFactory{

    public MkUserFactory() {
        super( "Make User", new String[]{"mkuser","makeuser"} );
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new MkUser(args, host, currentDir,resourceFactory);
    }

}
