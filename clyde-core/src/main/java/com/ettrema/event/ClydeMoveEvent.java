package com.ettrema.event;

import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;

/**
 * Called after moving, but allows discovering the new and previous parent collections
 *
 * @author brad
 */
public class ClydeMoveEvent implements ResourceEvent{
    private final BaseResource res;
    private final Folder oldParent;

    public ClydeMoveEvent( BaseResource res, Folder oldParent ) {
        this.res = res;
        this.oldParent = oldParent;
    }

    @Override
    public BaseResource getResource() {
        return res;
    }

    public Folder getOldParent() {
        return oldParent;
    }
}
