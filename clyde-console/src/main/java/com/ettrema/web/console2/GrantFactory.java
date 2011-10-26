package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class GrantFactory extends AbstractFactory {

    public GrantFactory() {
        super( "Grant a role to a user for a resource. Usage: grant [roleName] [userName[@userHost]] [srcPath]", new String[]{"grant"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Grant(args, host, currentDir,resourceFactory);
    }
}