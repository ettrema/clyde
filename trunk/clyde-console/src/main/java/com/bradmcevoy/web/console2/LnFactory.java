package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class LnFactory extends AbstractFactory {

    public LnFactory() {
        super( "Create a link to a folder. Usage: ln [newName] [destinationPath]", new String[]{"ln","link"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Ln(args, host, currentDir,resourceFactory);
    }
}