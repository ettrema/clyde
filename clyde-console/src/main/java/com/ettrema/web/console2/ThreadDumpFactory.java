package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class ThreadDumpFactory extends AbstractFactory {

    public ThreadDumpFactory() {
        super("Show a thread dump", new String[]{"thread"});
    }

    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth) {
        return new ThreadDump(args, host, currentDir, resourceFactory);
    }
}
