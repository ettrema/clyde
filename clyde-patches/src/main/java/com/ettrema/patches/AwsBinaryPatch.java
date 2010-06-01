package com.ettrema.patches;

import com.bradmcevoy.web.console2.PatchApplicator;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.vfs.BinaryMigrator;
import com.bradmcevoy.vfs.JdbcBinaryManager;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.ettrema.vfs.aws.AwsBinaryManager; 
import java.util.UUID;

/**
 *
 */
public class AwsBinaryPatch implements PatchApplicator {

    String[] args;

    private static final long serialVersionUID = 1L;

    public void setArgs(String[] args) {
        this.args = args;
    }
    
    public String getName() {
        return "Migrate binaries from postgres to S3";
    }

    public void doProcess(Context context) {
        VfsSession sess = context.get(VfsSession.class);

        JdbcBinaryManager from = new JdbcBinaryManager();
        AwsBinaryManager to = new AwsBinaryManager(null);

        NameNode node;
        if( args == null || args.length == 0 ) {
            node = sess.root();
        } else {
            String sId = args[0];
            UUID id = UUID.fromString(sId);
            node = sess.get(id);
            if( node == null ) throw new RuntimeException("Could not find node: " + id);
        }

        BinaryMigrator migrator = new BinaryMigrator(from, to);
        migrator.migrate(node, -1);
    }

    public void pleaseImplementSerializable() {

    }

}
