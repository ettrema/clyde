package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.media.MediaLogService;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.wall.WallService;
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
        int folders = crawl( f, proc, skipIfExists );

        return result( "Processing folders: " + folders );
    }

    private int crawl( Folder f, AsynchProcessor proc, boolean skipIfExists ) {
        log.warn( "crawl: " + f.getHref() );
        int cnt = 1;
        ThumbGenerator gen = new ThumbGenerator( f.getNameNodeId(), skipIfExists, f.getPath().toString() );
        proc.enqueue( gen );

        for( Resource r : f.getChildren() ) {
            if( r instanceof Folder ) {
                Folder fChild = (Folder) r;
                if( !fChild.isSystemFolder() ) {
                    cnt += crawl( fChild, proc, skipIfExists );
                }
            }
        }
        return cnt;
    }

    public static class ThumbGenerator extends VfsCommon implements Processable {

        final UUID folderId;
        private static final long serialVersionUID = 1L;
        private final boolean skipIfExists;
        private final String path;

        public ThumbGenerator( UUID folderId, boolean skipIfExists, String path ) {
            this.folderId = folderId;
            this.skipIfExists = skipIfExists;
            this.path = path;
        }

        @Override
        public void doProcess( Context context ) {
            log.warn("starting: " + this);
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
                    int numThumbs = imageFile.generateThumbs( skipIfExists );
                    notifyWallEtc( numThumbs, imageFile );
                }
            }
            commit();
            log.warn("finished: " + this);
        }

        private void notifyWallEtc( int numThumbs, BinaryFile file ) {
            MediaLogService mediaLogService = requestContext().get( MediaLogService.class );
            WallService wallService = requestContext().get( WallService.class );
            if( file.getParent().isSystemFolder() ) {
                log.trace( "parent is sys folder: " + file.getParent().getUrl() );
                return;
            }
            if( numThumbs > 0 ) {
                if( mediaLogService != null ) {
                    mediaLogService.onThumbGenerated( file );
                }

                if( wallService != null ) {
                    log.trace( "updating wall" );
                    wallService.onUpdatedFile( file );
                }
            } else {
                log.trace( "not checking thumbs because no thumbs generated" );
            }
        }

        @Override
        public String toString() {
            return "Crawler: " + path;
        }

        public void pleaseImplementSerializable() {
        }
    }
}
