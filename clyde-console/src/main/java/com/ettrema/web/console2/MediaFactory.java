package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.web.wall.WallService;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

/**
 *
 * @author brad
 */
public class MediaFactory extends AbstractFactory{

    private final WallService wallService;

    public MediaFactory(WallService wallService) {
        super( "collection of media commands", new String[]{"media"} );
        this.wallService = wallService;
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Media( args, host, currentDir, resourceFactory, wallService );
    }
}
