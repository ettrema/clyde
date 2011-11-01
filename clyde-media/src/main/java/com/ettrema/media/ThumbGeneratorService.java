package com.ettrema.media;

import java.io.FileNotFoundException;
import java.io.IOException;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.BaseResource;
import com.ettrema.utils.LogUtils;
import com.ettrema.video.FlashService;
import com.ettrema.web.BinaryFile;
import com.ettrema.web.FlashFile;
import com.ettrema.web.Folder;
import com.ettrema.web.ImageFile;
import com.ettrema.event.EventManager;
import com.ettrema.web.Thumb;
import com.ettrema.web.VideoFile;
import com.ettrema.web.wall.WallService;
import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.RootContextLocator;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.vfs.CommitListener;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsProvider;
import com.ettrema.vfs.VfsSession;
import java.util.List;
import java.util.UUID;


/**
 * This service is responsible for causing thumbnail generation to occur, although
 * the actual processing of thumbs occurs elsewhere
 *
 * @author brad
 */
public class ThumbGeneratorService implements Service, CommitListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThumbGeneratorService.class );
	private final VfsProvider vfsProvider;
	private final EventManager eventManager;
	private final ThumbProcessor thumbProcessor;
	private final AsynchProcessor asynchProc;
	private final FlashService flashService;
    private MediaLogService mediaLogService;
    private WallService wallService;

    public ThumbGeneratorService( VfsProvider vfsProvider, EventManager eventManager, ThumbProcessor thumbProcessor, AsynchProcessor asynchProc, FlashService flashService ) {
		this.vfsProvider = vfsProvider;
		this.eventManager = eventManager;
		this.thumbProcessor = thumbProcessor;
		this.asynchProc = asynchProc;
		this.flashService = flashService;
    }

	@Override
    public void start() {
        log.info( "Starting thumbnail generator.." );
        vfsProvider.addCommitListener( this );
    }

	@Override
    public void stop() {
    }


	@Override
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
                if( dn instanceof ImageFile ) {
					log.trace( "enqueuing image" );
                    if( enqueue( (ImageFile) dn ) ) return;
                } else if( dn instanceof VideoFile ) {
					log.trace( "enqueuing video" );
                    if( enqueue( (VideoFile) dn ) ) return;					
                } else if( dn instanceof FlashFile ) {
					log.trace( "enqueuing flash" );
                    if( enqueue( (FlashFile) dn ) ) return;
                } else {
					LogUtils.trace(log, "Unsupported file type", dn.getClass());
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
		List<Thumb> thumbs = Thumb.getThumbSpecs( targetPage.getParent() );
		int num;
        try {
            num = thumbProcessor.generateThumbs( targetPage,thumbs, false );
        } catch( FileNotFoundException ex ) {
            throw new RuntimeException( targetPage.getHref(), ex );
        } catch( IOException ex ) {
            throw new RuntimeException( targetPage.getHref(),ex );
        }
    		
        //int num = targetPage.generateThumbs();
        return num;
    }

    private int generate( VideoFile videoFile ) {
        int num;
		try {
			num = flashService.generateStreamingVideo( videoFile );
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
        int num = flashService.generateThumbs( flashFile );
        return num;
    }

    private void notifyWallEtc( int numThumbs, BinaryFile file ) {
        if( file.getParent().isSystemFolder() ) {
            log.trace( "parent is sys folder: " + file.getParent().getUrl() );
            return;
        }
        if( numThumbs > 0 ) {
			ThumbGeneratedEvent event = new ThumbGeneratedEvent(file);
			try {
				// Will probably fire WallServiceImpl and MediaLogService
				eventManager.fireEvent(event);
			} catch (ConflictException ex) {
				log.warn("exception on event", ex);
			} catch (BadRequestException ex) {
				log.warn("exception on event", ex);
			} catch (NotAuthorizedException ex) {
				log.warn("exception on event", ex);
			}			
        } else {
            log.trace( "not checking thumbs because no thumbs generated" );
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
