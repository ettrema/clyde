package com.ettrema.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.web.Folder;
import com.ettrema.web.Web;
import com.ettrema.web.wall.WallService;
import com.ettrema.console.Result;
import java.util.List;

/**
 * Collection of functions for managing media
 *
 * @author brad
 */
public class Media extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Mk.class );
    private final WallService wallService;

    Media( List<String> args, String host, String currentDir, ResourceFactory resourceFactory, WallService wallService ) {
        super( args, host, currentDir, resourceFactory );
        this.wallService = wallService;
    }

    @Override
    public Result execute() {
        String cmd = args.get( 0 );
        if( cmd.equals( "wall" ) ) {
            return doWall();
        } else {
            return result( "unknown command: " + cmd );
        }
    }

    private Result doWall() {
        String cmd = args.get( 1 );
        if( cmd == null ) {
            return result("please enter a media command, eg clearwall");
        } else if( cmd.equals( "clearwall" ) ) {
            Folder f = this.currentResource();
            if( f instanceof Web ) {
                Web web = (Web) f;
                log.trace( "clearing wall: " + web.getHref() );
                wallService.clearWall( web );
                commit();
                return result( "cleared wall of: " + web.getHref() );
            } else {
                return result( "current resource is not a Web" );
            }
        } else {
            return result("unrecognise media command: " + cmd);
        }
    }
}
