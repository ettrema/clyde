package com.bradmcevoy.media;

import com.bradmcevoy.event.Event;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.event.EventListener;
import com.bradmcevoy.event.EventManager;
import com.bradmcevoy.event.PostSaveEvent;
import com.bradmcevoy.thumbs.ThumbSelector;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Thumb;
import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.RequestContext;
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
 *
 * @author brad
 */
public class ThumbGeneratorService implements Service, CommitListener, EventListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThumbGeneratorService.class );

    private final ThumbSelector thumbSelector;
    private final RootContextLocator rootContextLocator;

    public ThumbGeneratorService( RootContextLocator rootContextLocator,ThumbSelector thumbSelector ) {
        this.thumbSelector = thumbSelector;
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
        if( e instanceof PostSaveEvent ) {
            PostSaveEvent pse = (PostSaveEvent) e;
            if( pse.getResource() instanceof ImageFile ) {
                ImageFile img = (ImageFile) pse.getResource();
                log.debug( "check to see if parentFolder needs thumbHref set: " + img.getHref() );
                Folder parentFolder = img.getParentFolder();
                if( parentFolder.getName().equals( "thumbs" ) ) {
                    log.debug("parent folder is thumbs, so check its parent");
                    parentFolder = parentFolder.getParent();
                    log.debug( "is a thumb, so check parent parent");
                    if( thumbSelector.checkThumbHref( parentFolder ) ) {
                        log.debug( "folder modified by checking thumbs" );
                        parentFolder.save();
                    }
                }
            }
        }
    }

    public void destroy() {
    }

    public void onRemove( Object item ) {
    }

    public void onCommit( NameNode n ) throws Exception {
        DataNode dn = n.getData();
        if( dn instanceof ImageFile ) {
            RequestContext context = RequestContext.getCurrent();
            ImageFile f = (ImageFile) dn;
            if( f.getParentFolder() != null ) {
                List<Thumb> thumbSpecs = Thumb.getThumbSpecs( f.getParentFolder() );
                if( thumbSpecs == null || thumbSpecs.size() == 0 ) return;
                ThumbnailGeneratorProcessable proc = new ThumbnailGeneratorProcessable( n.getId(), n.getName() );
                AsynchProcessor asynchProc = context.get( AsynchProcessor.class );
                asynchProc.enqueue( proc );
            } else {
                log.warn( "image has no parent folder! " + f.getName() );
            }
        }
    }

    public void processGenerator( Context context, String targetName, UUID imageFileNameNodeId) {
        log.debug( "generating thumbs: " + targetName + "..." );
        VfsSession vfs = context.get( VfsSession.class );
        NameNode pageNameNode = vfs.get( imageFileNameNodeId );
        if( pageNameNode == null ) {
            ThumbGeneratorService.log.debug( "..name node not found. prolly deleted: " + targetName );
            return;
        }
        DataNode dn = pageNameNode.getData();
        if( dn == null ) {
            log.warn( "Could not find target: " + imageFileNameNodeId );
            return;
        }
        ImageFile targetPage;
        if( dn instanceof ImageFile ) {
            targetPage = (ImageFile) dn;
        } else {
            log.warn( "Target page is not of type CommonTemplated. Is a: " + dn.getClass().getName() );
            return;
        }
        try {
            int count = generate( targetPage );
            if( count > 0 ) {
                vfs.commit();
            } else {
                vfs.rollback();
            }
        } catch( Exception e ) {
            // consume exception so we don't keep trying to process same message
            ThumbGeneratorService.log.error( "failed to generate thumbs for: " + targetPage.getHref(), e );
            vfs.rollback();
        }
    }

    /**
     *
     * @param targetPage
     * @return - number of thumbs generated
     */
    private int generate( ImageFile targetPage ) {
        log.debug( "...doing generation..." );
        int num = targetPage.generateThumbs();
        if( num > 0 ) {
            Folder parentFolder = targetPage.getParentFolder();
            log.debug( "checking thumbs: " + parentFolder.getHref() );
            thumbSelector.checkThumbHref( parentFolder );
        } else {
            ThumbGeneratorService.log.debug( "not checking thumbs because no thumbs generated" );
        }
        return num;
    }

}
