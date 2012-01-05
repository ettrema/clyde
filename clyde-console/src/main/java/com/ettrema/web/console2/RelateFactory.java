package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class RelateFactory extends AbstractFactory {

    public RelateFactory() {
        super("View and create relations", new String[]{"relate", "rel"});
    }

    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth) {
        return new Relate(args, host, currentDir, resourceFactory);
    }
}
