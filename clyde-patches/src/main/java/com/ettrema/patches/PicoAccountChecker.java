package com.ettrema.patches;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.console2.PatchApplicator;
import com.ettrema.context.Context;
import com.ettrema.vfs.VfsSession;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class PicoAccountChecker implements PatchApplicator {

    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThumbChecker.class );
    /**
     * Null for root item, set for subsequent nodes
     */
    private UUID nodeId;
    private Folder currentFolder;

    public PicoAccountChecker() {
    }

    public void setArgs( String[] args ) {
        if( args != null && args.length > 0 ) {
            String s = args[0];
            this.nodeId = UUID.fromString( s );
            log.debug( "set nodeId: " + this.nodeId );
        }
    }

    public String getName() {
        return "Check that accounts have been setup correctly";
    }

    public void doProcess( Context context ) {
        VfsSession sess = context.get( VfsSession.class );

        for( Resource r : currentFolder.getChildren() ) {
            if( r instanceof Host ) {
                checkHost( (Host) r );
            }
        }

        sess.commit();
    }

    public void setCurrentFolder( Folder currentResource ) {
        currentFolder = currentResource;
    }

    public void pleaseImplementSerializable() {
    }

    private void checkHost( Host host ) {
        log.warn( "Check host: " + host.getName());
        if( host.getCreator() == null  ) {
            log.error( "***************** No Creator for host: " + host.getName());
        }
        Resource rBlogs = host.child( "blogs" );
        if( rBlogs == null ) {
            ITemplate t = host.getTemplate( "blogHome" );
            Folder blogs = t.createFolderFromTemplate( host, "blogs" );
            blogs.save();
            log.warn("created blogs folder: " + blogs.getHref());
        } else {
            Folder fBlogs = (Folder) rBlogs;
            if( !"blogHome".equals( fBlogs.getTemplateName() ) ) {
                fBlogs.setTemplateName( "blogHome");
                fBlogs.save();
                log.warn("set template on blogs: " + fBlogs.getHref());
            }
        }


    }
}
