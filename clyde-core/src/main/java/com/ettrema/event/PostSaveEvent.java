package com.ettrema.event;

import com.bradmcevoy.http.Resource;
import com.ettrema.event.ResourceEvent;

/**
 *
 * @author brad
 */
public class PostSaveEvent implements ResourceEvent{
    private final Resource res;

    public PostSaveEvent( Resource res ) {
        this.res = res;
    }

    @Override
    public Resource getResource() {
        return res;
    }


}
