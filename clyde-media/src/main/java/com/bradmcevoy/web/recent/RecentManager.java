package com.bradmcevoy.web.recent;

import com.bradmcevoy.event.DeleteEvent;
import com.bradmcevoy.event.Event;
import com.bradmcevoy.event.EventListener;
import com.bradmcevoy.event.EventManager;
import com.bradmcevoy.event.PostSaveEvent;
import com.bradmcevoy.web.BaseResource;

/**
 *
 */
public class RecentManager implements EventListener{

    private RecentResourceCreator creator;

    public RecentManager(EventManager eventManager) {
        this.creator = new RecentResourceCreator();
        eventManager.registerEventListener(this, PostSaveEvent.class);
        eventManager.registerEventListener(this, DeleteEvent.class);
    }

    public void onEvent(Event e) {
        if(e instanceof PostSaveEvent) {
            PostSaveEvent pse = (PostSaveEvent) e;
            BaseResource res = pse.getResource();
            if( res instanceof RecentResource) {
                return ;
            }
            creator.create(res);
        } else if( e instanceof DeleteEvent ) {
            DeleteEvent de = (DeleteEvent) e;
            BaseResource res = de.getResource();
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
