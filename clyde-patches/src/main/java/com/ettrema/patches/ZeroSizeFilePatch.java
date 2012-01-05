package com.ettrema.patches;

import com.ettrema.web.BinaryFile;
import com.ettrema.web.Folder;
import com.ettrema.web.console2.PatchApplicator;
import com.ettrema.context.Context;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class ZeroSizeFilePatch implements PatchApplicator {

    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThumbChecker.class );
    /**
     * Null for root item, set for subsequent nodes
     */
    private UUID nodeId;
    private String parentPath;

    public ZeroSizeFilePatch() {
        parentPath = "";
    }

    public ZeroSizeFilePatch( String parentPath, UUID nodeId ) {
        this.nodeId = nodeId;
        this.parentPath = parentPath;
    }

    public void setArgs( String[] args ) {
        if( args != null && args.length > 0 ) {
            String s = args[0];
            this.nodeId = UUID.fromString( s );
            log.debug( "set nodeId: " + this.nodeId );
        }
    }

    public String getName() {
        return "Check that thumbnails have been generated and folder thumb is set";
    }

    public void doProcess( Context context ) {
        VfsSession sess = context.get( VfsSession.class );

        NameNode node;
        if( nodeId == null ) {
            node = sess.root();
        } else {
            node = sess.get( nodeId );
            if( node == null ) {
                throw new RuntimeException( "Could not find node: " + nodeId );
            }
        }

        check( context, node );
        sess.commit();
    }

    public void pleaseImplementSerializable() {
    }

    private void check( Context context, NameNode node ) {
        AsynchProcessor asynchProc = context.get( AsynchProcessor.class );
        String childsParentPath = parentPath + "/" + node.getName();
        List<NameNode> list = new ArrayList( node.children() );
        for( NameNode child : list ) {
            DataNode dn = child.getData();
            if( dn instanceof Folder ) {
                Folder f = (Folder) dn;
                log.debug( "emqueue folder: " + f.getHref() );
                ZeroSizeFilePatch checker = new ZeroSizeFilePatch( childsParentPath, child.getId() );
                asynchProc.enqueue( checker );
            } else if( dn instanceof BinaryFile) {
                processBinaryFile((BinaryFile)dn);
            }
        }
        DataNode dn = node.getData();
        if( dn instanceof Folder ) {
            Folder f = (Folder) dn;
            log.debug( "process folder: " + f.getHref() );
            processFolder(f);
        }
    }

    private void processFolder( Folder f ) {

    }

    private void processBinaryFile( BinaryFile f ) {
        int persisted = f.getActualPersistedContentSize();
        if( persisted == 0) {
            log.error( "Found zero length binary file: " + f.getHref());
            f.deletePhysically();
        } else {
            Long l = f.getContentLength();
            if( l != null ) {
                int exp = (int) l.longValue();
                if( exp != persisted) {
                    log.error( "Found mismatching file sizes: expected: " + exp + " actual: " + persisted + " " + f.getHref() );
                }
            }
        }
    }

    public void setCurrentFolder( Folder currentResource ) {

    }
}


