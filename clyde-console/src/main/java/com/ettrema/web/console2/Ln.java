package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.LinkedFolder;
import com.ettrema.web.User;
import com.ettrema.console.Result;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class Ln extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Ln.class );

    public Ln( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        String newName = args.get( 0 );
        String linkTo = args.get( 1 );
        log.debug( "link: " + newName + "->" + linkTo );
        Folder curFolder = currentResource();
        if( curFolder == null ) {
            log.debug( "current folder not found: " + currentDir );
            return result( "current dir not found: " + currentDir );
        } else {
            Path pSrc = Path.path( linkTo );
            BaseResource target = (BaseResource) curFolder.find( pSrc );
            if( target == null ) {
                return result( "Couldnt find: " + pSrc );
            }
            if( target instanceof Folder ) {
                Folder targetFolder = (Folder) target;
                LinkedFolder ln = new LinkedFolder( curFolder, newName);
                ln.save();
                ln.setLinkedTo( targetFolder );
                commit();
                return result( "Created link" );
            } else {
                return result( "Target is not a Folder. Is a: " + target.getClass().getCanonicalName() );
            }
        }
    }

    private User findUser( BaseResource target, String userHostName, String userName ) {
        Host h = target.getHost();
        while( h != null ) {
            if( h.getName().equals( userHostName ) ) {
                return h.findUser( userName );
            }
            h = h.getParentHost();
        }
        return null;
    }

    private List<User> findUsers( BaseResource target, String userHostName ) {
        Host h = target.getHost();
        while( h != null ) {
            if( h.getName().equals( userHostName ) ) {
                List<User> users = new ArrayList<User>();
                for( Resource r : h.getUsers().getChildren() ) {
                    if( r instanceof User ) {
                        users.add( (User) r );
                    }
                }
                return users;
            }
            h = h.getParentHost();
        }
        return null;

    }
}
