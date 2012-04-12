package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.utils.CurrentRequestService;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.security.PermissionChecker;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.console.Result;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ettrema.context.RequestContext._;
import com.ettrema.vfs.VfsTransactionManager;

/**
 *
 * @author brad
 */
public class Mv extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Mv.class );

    public Mv( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    @Override
    public Result execute() {
        String sSourcePath = args.get( 0 );
        String sDestPath = args.get( 1 );

        NameNode dest;
        try {
            UUID uuid = UUID.fromString( sDestPath );
            VfsSession sess = _( VfsSession.class );
            dest = sess.get( uuid );
            if( dest == null ) {
                return result( "No such node: " + uuid );
            }
            if( !( dest.getData() instanceof Folder ) ) {
                return result( "destination is not a folder" );
            }
        } catch( IllegalArgumentException e ) {
            log.debug( "not a valid uuid: " + e.getMessage() + " - " + sDestPath );
            Path path = Path.path( sDestPath );
            List<BaseResource> list = new ArrayList<>();
            Resource rDest;
            try {
                rDest = find( path );
            } catch (    NotAuthorizedException | BadRequestException ex) {
                return result("Couldnt change path to: " + path, ex);
            }
            if( rDest instanceof Folder ) {
                Folder fDest = (Folder) rDest;
                dest = fDest.getNameNode();
            } else {
                return result( "destination isnt a folder" );
            }
        }
        Folder fDest = (Folder) dest.getData();
        Request req = _( CurrentRequestService.class ).request();
        if( req == null ) throw new RuntimeException( "No current request" );
        Auth auth = req.getAuthorization();
        boolean isSourceAuthor;
        try {
            isSourceAuthor = _( PermissionChecker.class ).hasRole( Role.AUTHOR, currentResource(), auth );
        } catch (NotAuthorizedException | BadRequestException ex) {
            return result("can't lookup current resource", ex);
        }
        if( !isSourceAuthor ) {
            return result( "You do not have the AUTHOR role on the source" );
        }
        boolean isDestAuthor = _( PermissionChecker.class ).hasRole( Role.AUTHOR, fDest, auth );
        if( !isDestAuthor ) {
            return result( "You do not have the AUTHOR role on the destination" );
        }


        // lookup source(s)

        try {
            UUID uuid = UUID.fromString( sSourcePath );
            VfsSession sess = _( VfsSession.class );
            NameNode sourceNode = sess.get( uuid );
            if( sourceNode == null ) {
                return result( "No such node: " + uuid );
            }
            sourceNode.move( dest, sourceNode.getName() );
            VfsTransactionManager.commit();
            return result( "moved: " + uuid );
        } catch( IllegalArgumentException e ) {
            try {
                // ok, not a uuid
                Path path = Path.path( sSourcePath );
                List<BaseResource> list = new ArrayList<>();
                Folder curFolder = currentResource();
                Result resultSearch = findWithRegex( curFolder, path, list );
                if( resultSearch != null ) {
                    return resultSearch;
                }

                if( list.isEmpty() ) {
                    return result( "source not found: " + sSourcePath );
                }
                StringBuilder sb = new StringBuilder();
                for( BaseResource r : list ) {
                    NameNode source = r.getNameNode();
                    source.move( dest, source.getName() );
                }
                commit();
                return result( "moved " + list.size() + " items" );
            } catch (    NotAuthorizedException | BadRequestException ex) {
                return result("can't lookup current resource", ex);
            }
        }

    }
}
