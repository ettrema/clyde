package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.console.Result;
import java.util.List;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;

/**
 *
 * @author brad
 */
public class Import extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Import.class );

    public Import( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        try {
            if( args.size() == 0 ) {
                return result( "not enough arguments" );
            }
            String importSource = args.get( 0 );
            String remoteUser = "";
            if( args.size() > 0 ) {
                remoteUser = args.get( 1 );
            }
            String remotePassword = "";
            if( args.size() > 1 ) {
                remotePassword = args.get( 2 );
            }
            return doImport( importSource, remoteUser, remotePassword );
        } catch( FileSystemException ex ) {
            log.error( "", ex );
            return result( "err: " + ex.getMessage() );
        }

    }

    private Result doImport( String importRoot, String remoteUser, String remotePassword ) throws FileSystemException {
        FileSystemManager fsManager = VFS.getManager();
        if( remoteUser.length() > 0 ) {
            log.debug( "user" + remoteUser );
            StaticUserAuthenticator auth = new StaticUserAuthenticator( null, remoteUser, remotePassword );
            FileSystemOptions opts = new FileSystemOptions();
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator( opts, auth );
        } else {
            log.debug( "no login details");
        }

        FileObject folder = fsManager.resolveFile( importRoot );

        log.debug( "got folder: " + folder.getName() );
        for( FileObject child : folder.getChildren() ) {
            log.debug( " - child: " + child.getName() );
        }

        return result( "ok" );
    }
}
