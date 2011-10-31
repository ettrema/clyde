package com.ettrema.event;

import com.ettrema.web.BaseResource;

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
