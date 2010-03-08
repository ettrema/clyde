package com.bradmcevoy.event;

import com.bradmcevoy.web.BaseResource;

/**
 *
 * @author brad
 */
public class DeleteEvent implements ClydeResourceEvent{
    private final BaseResource res;

    public DeleteEvent( BaseResource res ) {
        this.res = res;
    }

    @Override
    public BaseResource getResource() {
        return res;
    }

}
