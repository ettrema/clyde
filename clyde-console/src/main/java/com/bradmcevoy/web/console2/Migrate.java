package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.console.Result;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.BinaryMigrator;
import com.ettrema.vfs.JdbcBinaryManager;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import com.ettrema.vfs.aws.AwsBinaryManager;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class Migrate extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Migrate.class );
    private final JdbcBinaryManager from;
    private final AwsBinaryManager to;

    Migrate( List<String> args, String host, String currentDir, ResourceFactory resourceFactory, JdbcBinaryManager from, AwsBinaryManager to ) {
        super( args, host, currentDir, resourceFactory );
        this.from = from;
        this.to = to;
    }

    @Override
    public Result execute() {
        VfsSession sess = RequestContext.getCurrent().get( VfsSession.class );

        NameNode node;
        if( this.args.isEmpty() ) {
            return result( "please enter a node id to migrate" );
        } else {
            String sId = args.get( 0 );
            UUID id = UUID.fromString( sId );
            node = sess.get( id );
            if( node == null ) return result( "Could not find node: " + id );
        }

        if( !from.exists( node ) ) {
            return result( "source data does not exist" );
        }

        if( to.exists( node ) ) {
            from.delete( sess, node );
            commit();
            return result("dest already exists, so just removed source data");
        }

        BinaryMigrator migrator = new BinaryMigrator( from, to );
        if( migrator.migrate( node, -1 ) ) {
            commit();
            return result( "migrated ok" );
        } else {
            return result( "migration failed" );
        }

    }
}
