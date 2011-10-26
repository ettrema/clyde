
package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class RmFactory extends AbstractFactory {

    public RmFactory() {
        super( "Remove file", new String[]{"rm","delete","del"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Rm(args, host, currentDir,resourceFactory); 
    }

            
}
