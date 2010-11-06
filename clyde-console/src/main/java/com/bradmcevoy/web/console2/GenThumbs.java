package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.media.MediaLogService;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.Thumb;
import com.bradmcevoy.web.wall.WallService;
import com.ettrema.console.Result;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContextLocator;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author brad
 */
public class GenThumbs extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( GenThumbs.class );
    private final RootContextLocator rootContextLocator;
    private final int workers;
    private int runningWorkers;

    GenThumbs( List<String> args, String host, String currentDir, ResourceFactory resourceFactory, RootContextLocator rootContextLocator, int workers ) {
        super( args, host, currentDir, resourceFactory );
        this.rootContextLocator = rootContextLocator;
        this.workers = workers;
    }

    @Override
    public Result execute() {
        Resource r = currentResource();
        Folder f = (Folder) r;
        boolean skipIfExists = false;
        List<Thumb> thumbs = null;
        for( String s : args ) {
            if( s.equals( "-skipIfExists" ) ) {
                skipIfExists = true;
            } else {
                thumbs = Thumb.parseThumbs( s );
            }

        }
        java.util.Queue<Folder> folders = new ArrayBlockingQueue<Folder>( 20000 );
        long tm = System.currentTimeMillis();
        crawl( f, folders, skipIfExists );
        tm = System.currentTimeMillis() - tm;
        int numFolders = folders.size();
        log.warn( "crawled: " + numFolders + " in " + tm / 1000 + " secs" );

        for( int i = 0; i < workers; i++ ) {
            ThumbGenerator gen = new ThumbGenerator( folders, skipIfExists, f.getPath().toString(), thumbs );
            Thread thread = new Thread( gen );
            thread.setDaemon( true );
            thread.start();
        }


        return result( "Processing folders: " + numFolders + " running workers: " + runningWorkers );
    }

    private void crawl( Folder f, java.util.Queue<Folder> folders, boolean skipIfExists ) {
        log.warn( "crawl: " + f.getHref() );
        folders.add( f );
        for( Resource r : f.getChildren() ) {
            if( r instanceof Folder ) {
                Folder fChild = (Folder) r;
                if( !fChild.isSystemFolder() ) {
                    crawl( fChild, folders, skipIfExists );
                }
            }
        }
    }

    private boolean isDeprecatedThumbs( Folder fChild ) {
        return fChild.getName().equals( "regs" ) || fChild.getName().equals( "slideshows" ) || fChild.getName().equals( "thumbs" );
    }

    public class ThumbGenerator extends VfsCommon implements Runnable {

        final java.util.Queue<Folder> folders;
        private final boolean skipIfExists;
        private final List<Thumb> thumbs;

        public ThumbGenerator( java.util.Queue<Folder> folders, boolean skipIfExists, String path, List<Thumb> thumbs ) {
            this.folders = folders;
            this.skipIfExists = skipIfExists;
            this.thumbs = thumbs;
        }

        public void run() {
            int cnt = 0;
            runningWorkers++;
            try {
                while( !folders.isEmpty() ) {
                    final Folder f = folders.remove();
                    final int num = cnt++;
                    log.warn( "worker starting new job. Remaining workers: " + runningWorkers + " remaining queue: " + folders.size() );
                    rootContextLocator.getRootContext().execute( new Executable2() {

                        public void execute( Context context ) {
                            log.warn( "processing thumb item " + num + " of " + folders.size() );
                            doProcess( context, f.getNameNodeId() );
                        }
                    } );
                }
            } finally {
                runningWorkers--;
                log.warn( "worker completed. Remaining workers: " + runningWorkers + " remaining queue: " + folders.size() );
            }
        }

        public void doProcess( Context context, UUID folderId ) {
            long tm = System.currentTimeMillis();
            log.warn( "starting: " + folderId );
            String name = "unknown - " + folderId;
            int totalThumbs = 0;
            try {
                VfsSession session = context.get( VfsSession.class );
                NameNode nFolder = session.get( folderId );
                if( nFolder == null ) {
                    log.error( "Name node for host does not exist: " + folderId );
                    return;
                }
                name = nFolder.getName();
                Object data = nFolder.getData();
                if( data == null ) {
                    log.error( "Data node does not exist. Name node: " + folderId );
                    return;
                }
                if( !( data instanceof Folder ) ) {
                    log.error( "Node does not reference a Folder. Instead references a: " + data.getClass() + " ID:" + folderId );
                    return;
                }

                Folder folder = (Folder) data;
                name = folder.getPath().toString();
                if( isDeprecatedThumbs( folder ) ) {
                    log.warn("Found deprecated thumbs folder - DELETING: " + folder.getHref());
                    folder.delete();
                } else {
                    log.warn( "processing thumbs: " + name + " with thumb specs: " + Thumb.format( thumbs ) );
                    for( Resource r : folder.getChildren() ) {
                        if( r instanceof ImageFile ) {
                            ImageFile imageFile = (ImageFile) r;
                            log.trace( "process image file: " + imageFile.getPath() );
                            int numThumbs = imageFile.generateThumbs( skipIfExists, thumbs );
                            totalThumbs += numThumbs;
                            notifyWallEtc( numThumbs, imageFile );
                        } else {
                            log.trace( "not an imagefile" );
                        }
                    }
                }
                commit();
            } catch( Exception e ) {
                log.error( "exception generating thumbs", e );
                rollback();
            } finally {
                tm = System.currentTimeMillis() - tm;
                log.warn( "generated: " + totalThumbs + " thumbs in " + tm / 1000 + "secs for: " + name );
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

        public void pleaseImplementSerializable() {
        }
    }
}
