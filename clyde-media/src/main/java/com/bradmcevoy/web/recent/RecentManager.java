package com.bradmcevoy.web.recent;

import com.bradmcevoy.event.DeleteEvent;
import com.bradmcevoy.event.Event;
import com.bradmcevoy.event.EventListener;
import com.bradmcevoy.event.EventManager;
import com.bradmcevoy.event.PostSaveEvent;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Thumb;
import com.bradmcevoy.web.Web;
import java.util.List;

/**
 *
 */
public class RecentManager implements EventListener{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentManager.class);

    private RecentResourceCreator creator;

    public RecentManager(EventManager eventManager) {
        this.creator = new RecentResourceCreator();
        eventManager.registerEventListener(this, PostSaveEvent.class);
        eventManager.registerEventListener(this, DeleteEvent.class);
    }

    public void onEvent(Event e) {
        log.trace("onEvent");
        if(e instanceof PostSaveEvent) {
            log.trace("onEvent: PostSaveEvent");
            if(HttpManager.request() == null ) {
                log.debug("no current request, so dont create a recent file");
                return ;
            } else if( HttpManager.request().getAuthorization() == null ) {
                log.debug("no current auth");
                return ;
            } else if( HttpManager.request().getAuthorization().getTag() == null ) {
                log.debug("no current user");
                return ;
            }

            PostSaveEvent pse = (PostSaveEvent) e;
            if( !(pse.getResource() instanceof BaseResource) ) {
                log.trace( "not a baseresource");
                return ;
            }
            BaseResource res = (BaseResource) pse.getResource();
            if( res instanceof RecentResource) {
                log.trace("not creating recent, because is a recent resource");
                return ;
            }
            if( res instanceof Web ){
                log.trace("not creating recent for Web");
                return ;
            }
            if( res instanceof Folder ) {
                Folder fNew = (Folder) res;
                if( fNew.getName().equals(Web.RECENT_FOLDER_NAME)) {
                    log.trace("name is " + Web.RECENT_FOLDER_NAME);
                    if( fNew.getParent() != null && fNew.getParent() instanceof Web ) {
                        log.trace("not creating recent because is the Recent folder");
                        return ;
                    }
                }
                if( isThumbs(fNew) ) { // don't create recent for thumb folders
                    log.debug("is in thumbs folder");
                    return ;
                }
            } else {
                if( isThumbs(res.getParent()) ) {  // don't create recent files for thumb nails
                    log.debug("is in thumbs folder");
                    return ;
                }
            }
            log.trace("crate recent resource");
            creator.create(res);
        } else if( e instanceof DeleteEvent ) {
            log.trace("onEvent: DeleteEvent");
            DeleteEvent de = (DeleteEvent) e;
            if( !(de.getResource() instanceof BaseResource)) {
                return ;
            }
            BaseResource res = (BaseResource) de.getResource();
            if( res instanceof RecentResource) {
                return ;
            }
            creator.onDelete(res);
        }
    }

    public RecentResourceCreator getCreator() {
        return creator;
    }

    public void setCreator(RecentResourceCreator creator) {
        this.creator = creator;
    }

    /**
     * True if the given folder is a thumbs folder
     * 
     * @param f
     * @return
     */
    private boolean isThumbs(Folder f) {
        List<Thumb> thumbs = Thumb.getThumbSpecs( f.getParent() );
        if( thumbs != null ) {
            for( Thumb t : thumbs ) {
                String thumbFolderName = t.getSuffix() + "s";
                if( f.getName().equals(thumbFolderName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
