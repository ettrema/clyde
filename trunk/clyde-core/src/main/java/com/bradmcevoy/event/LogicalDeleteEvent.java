package com.bradmcevoy.event;

import com.bradmcevoy.web.BaseResource;
import com.ettrema.event.ResourceEvent;

/**
 * Called when a resource is logically deleted, ie moved to trash
 *
 * @author brad
 */
public class LogicalDeleteEvent implements ResourceEvent{
    private final BaseResource res;

    public LogicalDeleteEvent( BaseResource res ) {
        this.res = res;
    }

    @Override
    public BaseResource getResource() {
        return res;
    }
}
