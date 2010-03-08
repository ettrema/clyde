package com.bradmcevoy.event;

import com.bradmcevoy.web.BaseResource;

/**
 *
 * @author brad
 */
public class PutEvent implements ClydeResourceEvent {
    private final BaseResource res;

    public PutEvent( BaseResource res ) {
        this.res = res;
    }

    @Override
    public BaseResource getResource() {
        return res;
    }


}
