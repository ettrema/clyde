package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class ImportFactory extends AbstractFactory{

    public ImportFactory() {
        super( "Import from a remote location", new String[]{"import","im"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Import(args,host,currentDir,resourceFactory);
    }

}
