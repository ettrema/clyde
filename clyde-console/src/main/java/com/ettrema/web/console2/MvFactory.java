package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class MvFactory extends AbstractFactory {

    public MvFactory() {
        super( "Move", new String[]{"mv","move"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Mv(args, host, currentDir,resourceFactory);
    }


}
