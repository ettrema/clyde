package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class GroupFactory extends AbstractFactory {
    public GroupFactory() {
        super( "Add users to a group. Usage: group add /users/brad", new String[]{"group"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Group(args, host, currentDir,resourceFactory);
    }
}
