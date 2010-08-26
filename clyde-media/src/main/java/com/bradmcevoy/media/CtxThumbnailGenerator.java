package com.bradmcevoy.media;

import com.bradmcevoy.event.Event;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.event.EventListener;
import com.bradmcevoy.event.EventManager;
import com.bradmcevoy.event.PostSaveEvent;
import com.bradmcevoy.thumbs.ThumbSelector;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Thumb;
import com.ettrema.context.Context;
import com.ettrema.context.Factory;
import com.ettrema.context.Registration;
import com.ettrema.context.RequestContext;
import com.ettrema.context.RootContext;
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

/**
 * On startup, registers self as a commitlistener. On commits of imagefiles
 * it creates an asynch job to generate thumbnails
 * 
 * @author brad
 */
public class CtxThumbnailGenerator implements Factory<Object>, CommitListener, EventListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CtxThumbnailGenerator.class );
    private static final long serialVersionUID = 1L;
    public static Class[] classes = {};

    public CtxThumbnailGenerator() {
    }

    public Class[] keyClasses() {
        return classes;
    }

    public String[] keyIds() {
        return null;
    }

    public Registration<Object> insert( RootContext context, Context requestContext ) {
        return null;
    }

    public void init( RootContext rootContext ) {
        log.info( "Starting thumbnail generator.." );
        VfsProvider vfsProvider = rootContext.get( VfsProvider.class );
        vfsProvider.addCommitListener( this );

        log.info( "registering to listen for save events" );
        EventManager eventManager = rootContext.get( EventManager.class );
        if( eventManager == null ) {
            throw new RuntimeException( "Not available in config: " + EventManager.class );
        }
        eventManager.registerEventListener( this, PostSaveEvent.class );
    }

    public void onEvent( Event e ) {
        if( e instanceof PostSaveEvent ) {
            PostSaveEvent pse = (PostSaveEvent) e;
            if( pse.getResource() instanceof ImageFile ) {
                ImageFile img = (ImageFile) pse.getResource();
                log.debug( "check to see if parentFolder needs thumbHref set: " + img.getHref() );
                ThumbSelector sel = new ThumbSelector( "thumbs" );
                Folder parentFolder = img.getParentFolder();
                if( parentFolder.getName().equals( "thumbs" ) ) {
                    log.debug("parent folder is thumbs, so check its parent");
                    parentFolder = parentFolder.getParent();
                    log.debug( "is a thumb, so check parent parent");
                    if( sel.checkThumbHref( parentFolder ) ) {
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

    public static class ThumbnailGeneratorProcessable implements Processable, Serializable {

        private static final long serialVersionUID = 1L;
        final String targetName;
        final UUID imageFileNameNodeId;

        public ThumbnailGeneratorProcessable( UUID imageFileNameNodeId, String name ) {
            this.targetName = name;
            this.imageFileNameNodeId = imageFileNameNodeId;
        }

        public void doProcess( Context context ) {
            log.debug( "generating thumbs: " + targetName + "..." );
            VfsSession vfs = context.get( VfsSession.class );
            NameNode pageNameNode = vfs.get( imageFileNameNodeId );
            if( pageNameNode == null ) {
                log.debug( "..name node not found. prolly deleted: " + targetName );
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
                log.error( "failed to generate thumbs for: " + targetPage.getHref(), e );
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
                ThumbSelector sel = new ThumbSelector( "thumbs" );
                Folder parentFolder = targetPage.getParentFolder();
                log.debug( "checking thumbs: " + parentFolder.getHref() );
                sel.checkThumbHref( parentFolder );
            } else {
                log.debug( "not checking thumbs because no thumbs generated" );
            }
            return num;
        }

        public void pleaseImplementSerializable() {
        }
    }
}
