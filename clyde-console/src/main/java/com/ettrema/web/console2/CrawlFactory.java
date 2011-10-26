
package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class CrawlFactory extends AbstractFactory{

    public CrawlFactory() {
        super("Crawl over all files and folders search indexing each item", new String[]{"crawl"} );
    }
    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Crawl(args,host,currentDir, resourceFactory);
    }
    
}
