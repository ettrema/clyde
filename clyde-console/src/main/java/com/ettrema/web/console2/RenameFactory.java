package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class RenameFactory extends AbstractFactory {

    public RenameFactory() {
        super( "Rename: Eg rn from to , note that from may be a UUID", new String[]{"rn","rename"});
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Mv(args, host, currentDir,resourceFactory);
    }


}
