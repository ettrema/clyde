package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class MkFactory extends AbstractFactory {

    public MkFactory() {
        super( "Make a Clyde resource", new String[]{"mk", "new"} );
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Mk( args, host, currentDir, resourceFactory );
    }
}
