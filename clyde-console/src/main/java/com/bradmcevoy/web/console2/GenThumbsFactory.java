package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import com.ettrema.context.RootContextLocator;
import java.util.List;

/**
 *
 * @author brad
 */
public class GenThumbsFactory extends AbstractFactory {

    private final RootContextLocator rootContextLocator;

    public GenThumbsFactory( RootContextLocator rootContextLocator ) {
        super( "Recursively search for image files which need thumbnails generated, option -skipIfExists", new String[]{"genthumbs"} );
        this.rootContextLocator = rootContextLocator;
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new GenThumbs( args, host, currentDir, resourceFactory, rootContextLocator );
    }
}
