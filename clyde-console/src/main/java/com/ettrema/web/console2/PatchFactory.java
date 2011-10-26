
package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class PatchFactory extends AbstractFactory {


    public PatchFactory() {
        super( "Patch. Execute a named patch", new String[]{"patch"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Patch(args, host, currentDir,resourceFactory);
    }

    

}
