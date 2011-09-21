package com.bradmcevoy.media;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.event.PostSaveEvent;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.video.FlashService;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.FlashFile;
import com.bradmcevoy.web.Folder;
import com.ettrema.event.Event;
import com.bradmcevoy.web.ImageFile;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import com.bradmcevoy.web.Thumb;
import com.bradmcevoy.web.VideoFile;
import com.bradmcevoy.web.wall.WallService;
import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.RootContextLocator;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.grid.Processable;
import com.ettrema.vfs.CommitListener;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsProvider;
import com.ettrema.vfs.VfsSession;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static com.ettrema.context.RequestContext._;

/**
 * This service is responsible for causing thumbnail generation to occur, although
 * the actual processing of thumbs occurs elsewhere
 *
 * @author brad
 */
public class ThumbGeneratorService implements Service, CommitListener, EventListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThumbGeneratorService.class );
    private final RootContextLocator rootContextLocator;
    private MediaLogService mediaLogService;
    private WallService wallService;

    public ThumbGeneratorService( RootContextLocator rootContextLocator ) {
        this.rootContextLocator = rootContextLocator;
    }

    public void start() {
        log.info( "Starting thumbnail generator.." );
        VfsProvider vfsProvider = rootContextLocator.getRootContext().get( VfsProvider.class );
        vfsProvider.addCommitListener( this );

        log.info( "registering to listen for save events" );
        EventManager eventManager = rootContextLocator.getRootContext().get( EventManager.class );
        if( eventManager == null ) {
            throw new RuntimeException( "Not available in config: " + EventManager.class );
        }
        eventManager.registerEventListener( this, PostSaveEvent.class );
    }

    public void stop() {
    }

    public void onEvent( Event e ) {
    }

    public void destroy() {
    }

    public void onRemove( Object item ) {
    }

    public void onCommit( NameNode n ) throws Exception {
        DataNode dn = n.getData();
        if( dn instanceof BinaryFile ) {
            BinaryFile bf = (BinaryFile) dn;
            Folder parent = bf.getParent();
            if( parent != null && parent.isSystemFolder() ) {
                if( log.isTraceEnabled() ) {
                    log.trace( "parent is a system folder,not generating: " + n.getName() );
                }
                return;
            } else {
                log.trace( "enqueuing" );
                if( dn instanceof ImageFile ) {
                    if( enqueue( (ImageFile) dn ) ) return;
                } else if( dn instanceof VideoFile ) {
                    if( enqueue( (VideoFile) dn ) ) return;
                } else if( dn instanceof FlashFile ) {
                    if( enqueue( (FlashFile) dn ) ) return;
                }
            }
        } else {
            if( log.isTraceEnabled() ) {
                log.trace( "not enqueing non-binary: "  + n.getName());
                if( dn == null ) {
                    log.trace( " -  null datanode on name node: " + n.getId() );
                } else {
                    log.trace( " - class is : " + dn.getClass() );
                }
            }
        }

    }

    public void initiateGeneration( Context context, String targetName, UUID fileNameNodeId ) {
        if( log.isTraceEnabled() ) {
            log.trace( "generating thumbs: " + targetName + "..." );
        }
        VfsSession vfs = context.get( VfsSession.class );
        NameNode pageNameNode = vfs.get( fileNameNodeId );
        if( pageNameNode == null ) {
            log.trace( "..name node not found. prolly deleted: " + targetName );
            return;
        }
        DataNode dn = pageNameNode.getData();
        if( dn == null ) {
            log.warn( "Could not find target: " + fileNameNodeId );
            return;
        } else if( dn instanceof BaseResource) {
            boolean didSomething = doGeneration((BaseResource)dn, vfs);
        try {
            if( didSomething ) {
                vfs.commit();
            } else {
                vfs.rollback();
            }
        } catch( Exception e ) {
            // consume exception so we don't keep trying to process same message
            log.error( "failed to generate thumbs for: " + fileNameNodeId, e );
            vfs.rollback();
        }            
        } else {
            log.warn( "Target is not a BaseResource: " + fileNameNodeId );
        }               
    }
    
    public boolean doGeneration(BaseResource dn, VfsSession vfs) {
        int count;
        if (dn instanceof ImageFile) {
            count = generate( (ImageFile) dn );
        } else if (dn instanceof VideoFile) {
            count = generate( (VideoFile) dn );
        } else if (dn instanceof FlashFile) {
            count = generate( (FlashFile) dn );
        } else {
            log.warn( "Target page is not a media file. Is a: " + dn.getClass().getName() );
            return false;
        }
        log.trace( "generated: " + count + " thumb files for: " + dn.getHref() );
        if( dn instanceof BinaryFile ) {
            notifyWallEtc( count, (BinaryFile) dn );
        }
        return count > 0;
    }

    private boolean enqueue( BinaryFile f ) {
        if( f.isTrash() ) {
            log.trace( "not generating thumbs because is in trash" );
        } else {
            if( f.getParentFolder() != null ) {
                List<Thumb> thumbSpecs = Thumb.getThumbSpecs( f.getParentFolder() );
                if( thumbSpecs == null || thumbSpecs.isEmpty() ) return true;
                ThumbnailGeneratorProcessable proc = new ThumbnailGeneratorProcessable( f.getNameNodeId(), f.getName() );
                AsynchProcessor asynchProc = _( AsynchProcessor.class );
                asynchProc.enqueue( proc );
            } else {
                log.warn( "image has no parent folder! " + f.getName() );
            }
        }
        return false;
    }


    /**
     *
     * @param targetPage
     * @return - number of thumbs generated
     */
    private int generate( ImageFile targetPage ) {
        int num = targetPage.generateThumbs();
        return num;
    }

    private int generate( VideoFile videoFile ) {
        int num;
		try {
			num = _( FlashService.class ).generateStreamingVideo( videoFile );
		} catch (NotAuthorizedException ex) {
			throw new RuntimeException(ex);
		} catch (ConflictException ex) {
			throw new RuntimeException(ex);
		} catch (BadRequestException ex) {
			throw new RuntimeException(ex);
		}
        return num;
    }

    private int generate( FlashFile flashFile ) {
        int num = _( FlashService.class ).generateThumbs( flashFile );
        return num;
    }

    private void notifyWallEtc( int numThumbs, BinaryFile file ) {
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

    public static class StreamingVideoProcessable extends VfsCommon implements Processable, Serializable {

        private static final long serialVersionUID = 1L;
        private final String sourceName;
        private final UUID id;

        public StreamingVideoProcessable( String sourceName, UUID id ) {
            this.sourceName = sourceName;
            this.id = id;
        }

        public void doProcess( Context context ) {
            log.debug( "processing: " + sourceName );
            VfsSession vfs = context.get( VfsSession.class );
            NameNode nn = vfs.get( id );
            if( nn == null ) {
                log.warn( "Couldnt find node: " + id );
                return;
            }
            DataNode data = nn.getData();
            if( data == null ) {
                log.warn( "node was found but datanode was null: name node id: " + id );
            } else if( data instanceof VideoFile ) {
                VideoFile file = (VideoFile) data;
                FlashService gen = _( FlashService.class );
                try {
                    gen.generateStreamingVideo( file );
                    commit();
                } catch( Exception e ) {
                    log.warn( "Exception generating streaming video: " + file.getHref(), e );
                    rollback();
                }
            } else {
                log.warn( "Not an instanceof video file: " + data.getClass() );
            }
        }

        public void pleaseImplementSerializable() {
        }
    }

    public MediaLogService getMediaLogService() {
        return mediaLogService;
    }

    public void setMediaLogService( MediaLogService mediaLogService ) {
        this.mediaLogService = mediaLogService;
    }

    public WallService getWallService() {
        return wallService;
    }

    public void setWallService( WallService wallService ) {
        this.wallService = wallService;
    }
}
