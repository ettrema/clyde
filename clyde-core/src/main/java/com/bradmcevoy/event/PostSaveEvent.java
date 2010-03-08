package com.bradmcevoy.event;

import com.bradmcevoy.web.BaseResource;

/**
 *
 * @author brad
 */
public class PostSaveEvent implements ClydeResourceEvent{
    private final BaseResource res;

    public PostSaveEvent( BaseResource res ) {
        this.res = res;
    }

    @Override
    public BaseResource getResource() {
        return res;
    }


}
