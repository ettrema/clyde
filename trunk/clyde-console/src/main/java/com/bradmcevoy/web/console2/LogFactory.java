
package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class LogFactory extends AbstractFactory{

    public LogFactory() {
        super("View the tail of the log file. Doesnt follow though", new String[]{"log","tail"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Log(args, host, currentDir,resourceFactory);
    }

}
