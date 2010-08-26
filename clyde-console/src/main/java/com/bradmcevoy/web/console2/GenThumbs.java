package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ImageFile;
import com.ettrema.console.Result;
import com.ettrema.context.Context;
import com.ettrema.context.RequestContext;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.grid.Processable;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class GenThumbs extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( GenThumbs.class );

    GenThumbs( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    @Override
    public Result execute() {
        Resource r = currentResource();
        Folder f = (Folder) r;
        AsynchProcessor proc = RequestContext.getCurrent().get( AsynchProcessor.class );
        boolean skipIfExists = false;
        if( args.size() > 0 ) {
            String opt = args.get( 0 );
            if( opt.equals( "-skipIfExists" ) ) {
                skipIfExists = true;
            }
        }
        int folders = crawl( f, proc,skipIfExists );

        return result( "Processing folders: " + folders );
    }

    private int crawl( Folder f, AsynchProcessor proc, boolean skipIfExists ) {
        log.warn( "crawl: " + f.getHref() );
        int cnt = 1;
        ThumbGenerator gen = new ThumbGenerator( f.getNameNodeId(), skipIfExists );
        proc.enqueue( gen );

        for( Resource r : f.getChildren() ) {
            if( r instanceof Folder ) {
                cnt += crawl( (Folder) r, proc, skipIfExists );
            }
        }
        return cnt;
    }

    public static class ThumbGenerator extends VfsCommon implements Processable {

        final UUID folderId;
        private static final long serialVersionUID = 1L;
        private final boolean skipIfExists;

        public ThumbGenerator( UUID folderId, boolean skipIfExists ) {
            this.folderId = folderId;
            this.skipIfExists = skipIfExists;
        }

        @Override
        public void doProcess( Context context ) {
            VfsSession session = context.get( VfsSession.class );
            NameNode nHost = session.get( folderId );
            if( nHost == null ) {
                log.error( "Name node for host does not exist: " + folderId );
                return;
            }
            Object data = nHost.getData();
            if( data == null ) {
                log.error( "Data node does not exist. Name node: " + folderId );
                return;
            }
            if( !( data instanceof Folder ) ) {
                log.error( "Node does not reference a Folder. Instead references a: " + data.getClass() + " ID:" + folderId );
                return;
            }

            Folder folder = (Folder) data;
            for( Resource r : folder.getChildren() ) {
                if( r instanceof ImageFile ) {
                    ImageFile imageFile = (ImageFile) r;
                    imageFile.generateThumbs(skipIfExists);
                }
            }
            commit();
        }

        @Override
        public String toString() {
            return "Crawler: " + folderId;
        }

        public void pleaseImplementSerializable() {
        }
    }
}

