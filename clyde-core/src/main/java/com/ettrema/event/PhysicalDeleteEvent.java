package com.bradmcevoy.event;

import com.bradmcevoy.web.BaseResource;
import com.ettrema.event.ResourceEvent;

/**
 * Called when a resource is physically deleted
 *
 * @author brad
 */
public class PhysicalDeleteEvent implements ResourceEvent{
    private final BaseResource res;

    public PhysicalDeleteEvent( BaseResource res ) {
        this.res = res;
    }

    @Override
    public BaseResource getResource() {
        return res;
    }
}
