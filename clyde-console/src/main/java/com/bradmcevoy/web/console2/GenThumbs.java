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
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContextLocator;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class GenThumbs extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( GenThumbs.class );
    private final RootContextLocator rootContextLocator;

    GenThumbs( List<String> args, String host, String currentDir, ResourceFactory resourceFactory, RootContextLocator rootContextLocator ) {
        super( args, host, currentDir, resourceFactory );
        this.rootContextLocator = rootContextLocator;
    }

    @Override
    public Result execute() {
        Resource r = currentResource();
        Folder f = (Folder) r;
        boolean skipIfExists = false;
        if( args.size() > 0 ) {
            String opt = args.get( 0 );
            if( opt.equals( "-skipIfExists" ) ) {
                skipIfExists = true;
            }
        }
        List<Folder> folders = new ArrayList<Folder>();
        long tm = System.currentTimeMillis();
        crawl( f, folders, skipIfExists );
        tm = System.currentTimeMillis() - tm;
        log.warn( "crawled: " + folders.size() + " in " + tm / 1000 + " secs" );

        ThumbGenerator gen = new ThumbGenerator( folders, skipIfExists, f.getPath().toString() );

        Thread thread = new Thread( gen );
        thread.setDaemon( true );
        thread.start();


        return result( "Processing folders: " + folders.size() );
    }

    private void crawl( Folder f, List<Folder> folders, boolean skipIfExists ) {
        log.warn( "crawl: " + f.getHref() );
        for( Resource r : f.getChildren() ) {
            if( r instanceof Folder ) {
                Folder fChild = (Folder) r;
                if( !fChild.isSystemFolder() ) {
                    folders.add( fChild );
                    crawl( fChild, folders, skipIfExists );
                }
            }
        }
    }

    public class ThumbGenerator extends VfsCommon implements Runnable {

        final List<Folder> folders;
        private final boolean skipIfExists;
        private final String path;

        public ThumbGenerator( List<Folder> folders, boolean skipIfExists, String path ) {
            this.folders = folders;
            this.skipIfExists = skipIfExists;
            this.path = path;
        }

        public void run() {
            int cnt = 0;
            for( final Folder f : folders ) {
                final int num = cnt++;
                rootContextLocator.getRootContext().execute( new Executable2() {

                    public void execute( Context context ) {
                        log.warn( "processing thumb item " + num + " of " + folders.size() );
                        doProcess( context, f.getNameNodeId() );
                    }
                } );
            }
        }

        public void doProcess( Context context, UUID folderId ) {
            long tm = System.currentTimeMillis();
            log.warn( "starting: " + this );
            int totalThumbs = 0;
            try {
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
                log.warn( "processing thumbs: " + folder.getHref() );
                for( Resource r : folder.getChildren() ) {
                    if( r instanceof ImageFile ) {
                        ImageFile imageFile = (ImageFile) r;
                        int numThumbs = imageFile.generateThumbs( skipIfExists );
                        totalThumbs += numThumbs;
                        notifyWallEtc( numThumbs, imageFile );
                    }
                }
                commit();
            } catch( Exception e ) {
                rollback();
            } finally {
                tm = System.currentTimeMillis() - tm;
                log.warn( "generated: " + totalThumbs + " thumbs in " + tm / 1000 + "secs" );
            }
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
