package com.bradmcevoy.web.wall;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Web;
import com.bradmcevoy.web.image.ThumbHrefService;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import com.ettrema.event.PutEvent;
import com.ettrema.vfs.NameNode;

/**
 *
 * @author brad
 */
public class WallService implements EventListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( WallService.class );
    private final ThumbHrefService thumbHrefService;
    private int maxWallSize = 50;

    public WallService( ThumbHrefService thumbHrefService, EventManager eventManager ) {
        this.thumbHrefService = thumbHrefService;
        eventManager.registerEventListener( this, PutEvent.class );
    }

    public void onEvent( Event e ) {
        log.trace( "put" );
        if( e instanceof PutEvent ) {
            onPut( (PutEvent) e );
        }
    }

    private Wall createWall( Web web ) {
        log.trace( "create" );
        Wall wall = new Wall();
        web.getNameNode().add( "_sys_wall", wall );
        log.debug( "wall data node id: " + wall.getId() );
        return wall;
    }

    private void onPut( PutEvent e ) {
        Wall wall = null;
        if( e.getResource() instanceof BaseResource ) {
            BaseResource res = (BaseResource) e.getResource();
            wall = onUpdatedFile( res );
        }
        if( wall != null ) {
            checkWallSize( wall );
        }
    }

    public void clearWall( BaseResource res ) {
        Web web = res.getWeb();
        Wall wall = getWall( web, false );
        if( wall == null ) {
            log.trace( "no wall found" );
        } else {
            wall.delete();
        }
    }

    public Wall onUpdatedFile( BaseResource res ) {
        log.trace( "processPut" );
        Web web = res.getWeb();
        Wall wall = getWall( web, true );
        String folderPath = res.getParent().getUrl();
        FolderUpdateWallItem updateWallItem = null;
        for( WallItem wallItem : wall ) {
            if( wallItem instanceof FolderUpdateWallItem ) {
                FolderUpdateWallItem fu = (FolderUpdateWallItem) wallItem;
                if( fu.getFolderPath().equals( folderPath ) ) {
                    log.trace( "found existing folder update item" );
                    updateWallItem = fu;                    
                    break;
                }
            }
        }

        if( updateWallItem == null ) {
            log.trace( "create new folder update wall item" );
            updateWallItem = new FolderUpdateWallItem( folderPath );
            wall.addItem( updateWallItem );
        }

        addUpdatedFile( res, updateWallItem );
        wall.save();
        return wall;
    }

    public Wall getWall( Web web ) {
        return getWall( web, false );
    }

    private Wall getWall( Web web, boolean create ) {
        log.trace( "getWall" );
        NameNode nn = web.getNameNode().child( "_sys_wall" );
        if( nn == null ) {
            log.trace( "no wall node" );
            if( create ) {
                return createWall( web );
            } else {
                log.trace( "Wall not found" );
                return null;
            }
        } else {
            Wall wall = (Wall) nn.getData();
            if( wall == null || wall.getId() == null ) {
                log.trace( "no data node or no id, delete and create a new one" );
                wall.getNameNode().delete();
                wall = createWall( web );
            } else {
                log.trace( "return existing wall" );
            }
            return wall;
        }
    }

    private void addUpdatedFile( BaseResource res, FolderUpdateWallItem fu ) {
        String thumbPath = thumbHrefService.getThumbPath( res );
        if( log.isTraceEnabled() ) {
            log.trace( "addUpdatedFile: " + res.getUrl() + " thumb:" + thumbPath );
        }
        fu.addUpdated( res.getModifiedDate(), res.getUrl(), thumbPath );
    }

    private void checkWallSize( Wall wall ) {
        if( wall.size() > maxWallSize ) {
            log.trace( "checkWallSize: Find oldest and remove" );
            WallItem oldest = null;
            for( WallItem item : wall ) {
                if( oldest == null ) {
                    oldest = item;
                } else if( item.getLastUpdated().before( oldest.getLastUpdated() ) ) {
                    oldest = item;
                }
            }
            wall.remove( oldest );
        }
        if( log.isTraceEnabled() ) {
            log.trace( "wall size is now: " + wall.size() );
        }
    }

    public int getMaxWallSize() {
        return maxWallSize;
    }

    public void setMaxWallSize( int maxWallSize ) {
        this.maxWallSize = maxWallSize;
    }
}
