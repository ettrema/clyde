package com.ettrema.web.console2;

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

    private int numWorkers = 1;

    private boolean updateWall;

    public GenThumbsFactory( RootContextLocator rootContextLocator ) {
        super( "Recursively search for image files which need thumbnails generated, option -skipIfExists", new String[]{"genthumbs"} );
        this.rootContextLocator = rootContextLocator;
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new GenThumbs( args, host, currentDir, resourceFactory, rootContextLocator, numWorkers, updateWall );
    }

    public int getNumWorkers() {
        return numWorkers;
    }

    public void setNumWorkers( int numWorkers ) {
        this.numWorkers = numWorkers;
    }

    public boolean isUpdateWall() {
        return updateWall;
    }

    public void setUpdateWall( boolean updateWall ) {
        this.updateWall = updateWall;
    }
}
