package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class ProcessScanFactory extends AbstractFactory {

    public ProcessScanFactory() {
        super( "Perform a process scan on the current folder", new String[]{"processscan"} );

    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new ProcessScan( args, host, currentDir, resourceFactory ); 
    }
}
