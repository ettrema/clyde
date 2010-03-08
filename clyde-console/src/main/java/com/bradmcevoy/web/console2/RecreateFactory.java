package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class RecreateFactory extends AbstractFactory{

    public RecreateFactory() {
        super("Recreate all the binary files in this folder with a particular extension. Not recursive. Eg recreate avi", new String[]{"recreate"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Recreate(args, host, currentDir,resourceFactory);
    }

}
