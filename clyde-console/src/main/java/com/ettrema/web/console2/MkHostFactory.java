
package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class MkHostFactory extends AbstractFactory{

    public MkHostFactory() {
        super( "Make Host", new String[]{"mkhost","makehost"} );
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new MkHost(args, host, currentDir,resourceFactory);
    }

}
