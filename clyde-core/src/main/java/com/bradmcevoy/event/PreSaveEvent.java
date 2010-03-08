package com.bradmcevoy.event;

import com.bradmcevoy.web.BaseResource;

/**
 *
 * @author brad
 */
public class PreSaveEvent implements ClydeResourceEvent{
    private final BaseResource res;

    public PreSaveEvent( BaseResource res ) {
        this.res = res;
    }

    @Override
    public BaseResource getResource() {
        return res;
    }


}
