package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class SetPasswordFactory extends AbstractFactory {

    public SetPasswordFactory() {
        super( "SetPassword: Usage setpwd joe supersecret", new String[]{"setpwd","pwd"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new SetPassword(args, host, currentDir,resourceFactory);
    }


}
