package com.ettrema.web.recent;

import com.ettrema.event.PostSaveEvent;
import com.ettrema.event.DeleteEvent;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import com.bradmcevoy.http.HttpManager;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.Web;

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

	@Override
    public void onEvent(Event e) {
        log.trace("onEvent");
        if(e instanceof PostSaveEvent) {
            log.trace("onEvent: PostSaveEvent");
            if(HttpManager.request() == null ) {
                log.trace("no current request, so dont create a recent file");
                return ;
            } else if( HttpManager.request().getAuthorization() == null ) {
                log.trace("no current auth");
                return ;
            } else if( HttpManager.request().getAuthorization().getTag() == null ) {
                log.trace("no current user");
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
            Folder parent = res.getParent();
            if( parent != null && parent.isSystemFolder())    {
                log.trace("not creating recent, because parent is a system folder");
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
}
