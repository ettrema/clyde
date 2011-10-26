package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.web.User;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class MvelFactory extends AbstractFactory {

    public MvelFactory() {
        super("Evaluate an expression with Mvel", new String[]{"mvel"});
    }

    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth) {
        return new Mvel(args, host, currentDir, (User) auth.getTag(), resourceFactory);
    }
}
