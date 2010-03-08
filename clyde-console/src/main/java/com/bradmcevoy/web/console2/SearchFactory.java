
package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class SearchFactory extends AbstractFactory{

    public SearchFactory() {
        super( "Search. Execute a full text search. All arguments are concatentated to a single search string", new String[]{"search"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Search(args, host, currentDir,resourceFactory);
    }

    
}
