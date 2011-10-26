
package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class AliasFactory extends  AbstractFactory{
    public AliasFactory() {
        super("Create an alias to an existing host. Eg alias realimage.co.nz /root/www.ettrema.com/www.realimage.co.nz", new String[]{"alias"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Alias(args, host, currentDir,resourceFactory);
    }

}
