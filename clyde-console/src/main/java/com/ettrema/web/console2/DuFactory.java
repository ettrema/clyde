
package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class DuFactory extends AbstractFactory{

    public DuFactory() {
        super("Disk Usage. Displays estimated disk usage for each item of the current folder", new String[]{"du"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Du(args, host, currentDir,resourceFactory);
    }    
}
