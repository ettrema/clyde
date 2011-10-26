package com.ettrema.web.recent;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.IUser;
import com.ettrema.web.Web;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class RecentResourceCreator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RecentResourceCreator.class );

    /**
     * Create a recent resource to represent the given resource which has been
     * created or updated
     *
     * If a recent resource exists for the same physical resource it should be
     * deleted.
     *
     * @param r
     * @return
     */
    public RecentResource create( BaseResource r ) {
        if( log.isDebugEnabled() ) {
            log.debug( "create recent resource: " + r.getHref() );
        }
        Web web = r.getWeb();
        Folder folder = web.getRecentFolder( true );

        IUser currentUser;
        Auth auth = HttpManager.request().getAuthorization();
        if( auth != null && auth.getTag() != null && auth.getTag() instanceof IUser ) {
            currentUser = (IUser) auth.getTag();
        } else {
            currentUser = null;
        }


        String recentName = r.getNameNodeId().toString();
        BaseResource existing = (BaseResource) folder.getChildResource( recentName );
        RecentResource rr;
        if( existing != null ) {
            log.debug( "found existing recent resource" );
            if( existing instanceof RecentResource ) {
                rr = (RecentResource) existing;
                rr.setUser( currentUser );
            } else {
                existing.deletePhysically();
                rr = new RecentResource( folder, r, currentUser );
            }
        } else {
            checkMaxRecent( folder );
            rr = new RecentResource( folder, r, currentUser );
        }
        rr.save();
        return null;
    }

    /**
     * Check if the recent list exceeds getRecentSize(), if so, delete the earliest
     * record
     * 
     * @param recentFolder - the Recent folder
     */
    private void checkMaxRecent( Folder recentFolder ) {
        List<? extends Resource> children = recentFolder.getChildren();
        if( children != null && children.size() > getMaxRecentSize() ) {
            Date earliestDate = new Date(); // now
            RecentResource toDelete = null;
            for( Resource child : children ) {
                if( child instanceof RecentResource ) {
                    Date childMod = child.getModifiedDate();
                    if( childMod != null && childMod.before( earliestDate ) ) {
                        earliestDate = childMod;
                        toDelete = (RecentResource) child;
                    }
                }
            }
            if( toDelete != null ) {
                try {
                    toDelete.deleteNoTx();
                } catch( NotAuthorizedException ex ) {
                    throw new RuntimeException( ex );
                } catch( ConflictException ex ) {
                    throw new RuntimeException( ex );
                } catch( BadRequestException ex ) {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    public void onDelete( BaseResource r ) {
        Web web = r.getWeb();
        Folder folder = web.getRecentFolder( true );
        String recentName = r.getNameNodeId().toString();
        BaseResource existing = (BaseResource) folder.getChildResource( recentName );
        if( existing != null ) {
            try {
                existing.delete();
            } catch( NotAuthorizedException ex ) {
                throw new RuntimeException( ex );
            } catch( ConflictException ex ) {
                throw new RuntimeException( ex );
            } catch( BadRequestException ex ) {
                throw new RuntimeException( ex );
            }
        }

    }

    private int getMaxRecentSize() {
        return 100;
    }
}
