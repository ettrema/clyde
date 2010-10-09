package com.ettrema.event;

import com.bradmcevoy.http.Resource;



/**
 *
 * @author brad
 */
public class PreSaveEvent implements ResourceEvent{
    private final Resource res;

    public PreSaveEvent( Resource res ) {
        this.res = res;
    }

    @Override
    public Resource getResource() {
        return res;
    }


}
