package com.ettrema.patches;

import com.bradmcevoy.web.console2.PatchApplicator;
import com.ettrema.context.Context;
import com.ettrema.vfs.BinaryMigrator;
import com.ettrema.vfs.JdbcBinaryManager;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import com.ettrema.vfs.aws.AwsBinaryManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class PurgeBinaryDataPatch implements PatchApplicator {

    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PurgeBinaryDataPatch.class );
    String[] args;

    public void setArgs( String[] args ) {
        this.args = args;
    }

    public String getName() {
        return "Delete migrated binary from jdbc";
    }

    public void doProcess( Context context ) {
        VfsSession sess = context.get( VfsSession.class );

        JdbcBinaryManager from = new JdbcBinaryManager();
        AwsBinaryManager to = new AwsBinaryManager( null );
        BinaryMigrator bm = new BinaryMigrator( from, to );

        NameNode node;
        if( args == null || args.length == 0 ) {
            throw new IllegalArgumentException( "must provide name node id to start from");
        } else {
            String sId = args[0];
            UUID id = UUID.fromString( sId );
            node = sess.get( id );
            if( node == null )
                throw new RuntimeException( "Could not find node: " + id );
        }

        Connection con = context.get( Connection.class );
        purge( node.getName(), node, from, to, sess, con, bm );
    }

    public void pleaseImplementSerializable() {
    }

    private void purge( String path, NameNode node, JdbcBinaryManager from, AwsBinaryManager to, VfsSession sess, Connection con, BinaryMigrator bm ) {
        log.debug( "purge: " + node.getId() );

//        if( from.exists( node ) ) {
//            if( to.exists( node ) ) {
//                log.debug( "deleting redundant from data" );
//                from.delete( sess, node );
//                commit( con );
//            } else {
//                log.debug( "data has not been migrated. migrating now...");
//                boolean didMigrate = bm._migrate( node, false);
//                if( !didMigrate ) throw new RuntimeException( "expected migration to occur");
//                log.debug( "migration completed, deleting source data...");
//                from.delete( sess, node );
//                log.debug( "done delete, commit");
//                commit( con );
//
//            }
//        }
//
//        for( NameNode child : node.children() ) {
//            purge( path + "/" + child.getName(), child, from, to, sess, con, bm );
//        }
    }

    private void commit( Connection con ) {
        try {
            con.commit();
        } catch( SQLException ex ) {
            log.error( "Couldnt commit", ex );
            throw new RuntimeException( ex );
        }
    }
}
