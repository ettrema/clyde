package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.console.Result;
import static com.ettrema.context.RequestContext.*;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class Rm extends AbstractConsoleCommand {

    public Rm( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        String s = args.get( 0 );
        boolean isDry;
        String sPath;
        if( "-dry".equals( s ) ) {
            isDry = true;
            sPath = args.get( 1 );
        } else {
            isDry = false;
            sPath = s;
        }

        try {
            UUID uuid = UUID.fromString( sPath );
            VfsSession sess = _( VfsSession.class );
            NameNode node = sess.get( uuid );
            if( node == null ) {
                return result( "No such node: " + uuid );
            }
            if( !isDry ) {
                node.delete();
                sess.commit();
                return result( "deleted: " + uuid );
            } else {
                return result( "would have deleted: " + uuid );
            }
        } catch( IllegalArgumentException e ) {
            // ok, not a uuid
            Path path = Path.path( sPath );
            List<BaseResource> list = new ArrayList<BaseResource>();
            Folder curFolder = currentResource();
            Result resultSearch = findWithRegex( curFolder, path, list );
            if( resultSearch != null ) {
                return resultSearch;
            }

            if( list.isEmpty() ) {
                return result( "not found: " + sPath );
            }
            StringBuilder sb = new StringBuilder();
            for( BaseResource r : list ) {
                try {
                    if( !isDry ) {
                        r._delete();
                    }
                    sb.append( r.getHref() ).append( "," );
                } catch( ConflictException ex ) {
                    throw new RuntimeException( ex );
                } catch( BadRequestException ex ) {
                    throw new RuntimeException( ex );
                } catch( NotAuthorizedException ex ) {
                    throw new RuntimeException( ex );
                }                
            }
            commit();
            return result( "deleted " + sb.toString() );
        }
    }
}
