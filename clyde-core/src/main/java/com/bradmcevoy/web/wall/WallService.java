package com.bradmcevoy.web.wall;

import com.bradmcevoy.event.LogicalDeleteEvent;
import com.bradmcevoy.event.PhysicalDeleteEvent;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.Web;
import com.bradmcevoy.web.image.ThumbHrefService;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import com.ettrema.event.PutEvent;
import com.ettrema.vfs.NameNode;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author brad
 */
public class WallService implements EventListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( WallService.class );
    private final ThumbHrefService thumbHrefService;
    private String thumbSuffix = "_sys_thumb";
    private int maxWallSize = 50;
    private List<String> excludedDirs = Arrays.asList( "/Recent/" );

    public WallService( ThumbHrefService thumbHrefService, EventManager eventManager ) {
        this.thumbHrefService = thumbHrefService;
        eventManager.registerEventListener( this, PutEvent.class );
        eventManager.registerEventListener( this, LogicalDeleteEvent.class );
        eventManager.registerEventListener( this, PhysicalDeleteEvent.class );
    }

    public void onEvent( Event e ) {
        if( e instanceof PutEvent ) {
            log.trace( "put" );
            onPut( (PutEvent) e );
        } else if( e instanceof LogicalDeleteEvent ) {
            log.trace( "logical delete" );
            onLogicalDelete( ( (LogicalDeleteEvent) e ).getResource() );
        } else if( e instanceof PhysicalDeleteEvent ) {
            log.trace( "physical delete" );
            onPhysicalDelete( ( (PhysicalDeleteEvent) e ).getResource() );
        }
    }

    private Wall createWall( BaseResource web ) {
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
            if( !isExcluded( res ) ) {
                wall = onUpdatedFile( res );
            }
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

    private void onLogicalDelete( BaseResource res ) {
        if( isExcluded( res ) ) {
            return;
        }
        Web web = res.getWeb();
        Wall wall = getWall( web, true );
        FileTrashedWallItem item = new FileTrashedWallItem( res.getUrl() );
        item.setLastUpdated( new Date() );
        wall.addItem( item );
        wall.save();
    }

    private void onPhysicalDelete( BaseResource res ) {
        if( isExcluded( res ) ) {
            return;
        }
        Web web = res.getWeb();
        Wall wall = getWall( web, true );
        FileDeletedWallItem item = new FileDeletedWallItem( res.getUrl() );
        item.setLastUpdated( new Date() );
        wall.addItem( item );
        wall.save();
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

    public Wall getUserWall( User user, boolean create ) {
        return getWall( user, create );
    }

    public Wall getWebWall( Web web, boolean create ) {
        return getWall( web, create );
    }

    private Wall getWall( BaseResource res, boolean create ) {
        log.trace( "getWall" );
        NameNode nn = res.getNameNode().child( "_sys_wall" );
        Wall wall = null;
        if( nn == null ) {
            log.trace( "no wall node" );
        } else {
            wall = (Wall) nn.getData();
            if( wall == null || wall.getId() == null ) {
                log.trace( "no data node or no id, delete and create a new one" );
                nn.delete();
            } else {
                log.trace( "return existing wall" );
            }
        }
        if( wall == null && create ) {
            wall = createWall( res );
        }
        return wall;
    }

    private void addUpdatedFile( BaseResource res, FolderUpdateWallItem fu ) {
        String thumbPath = thumbHrefService.getThumbPath( res, thumbSuffix );
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

    public List<String> getExcludedDirs() {
        return excludedDirs;
    }

    public void setExcludedDirs( List<String> excludedDirs ) {
        this.excludedDirs = excludedDirs;
    }

    private boolean isExcluded( BaseResource res ) {
        if( isExcluded( res.getUrl() ) ) {
            return true;
        }
        if( res instanceof Folder ) {
            Folder fRes = (Folder) res;
            if( fRes.isSystemFolder() ) {
                return true;
            }
        } else {
            Folder parent = res.getParent();
            if( parent == null ) {
                return false;
            } else {
                return parent.isSystemFolder();
            }
        }
        return false;
    }

    private boolean isExcluded( String pathFromHost ) {
        if( excludedDirs == null ) {
            return false;
        }
        for( String s : excludedDirs ) {
            if( pathFromHost.startsWith( s ) ) return true;
        }
        return false;
    }

    public String getThumbSuffix() {
        return thumbSuffix;
    }

    public void setThumbSuffix( String thumbSuffix ) {
        this.thumbSuffix = thumbSuffix;
    }
}
