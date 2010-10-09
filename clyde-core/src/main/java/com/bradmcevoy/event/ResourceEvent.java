package com.bradmcevoy.event;

import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public interface ResourceEvent extends Event {
    Resource getResource();
}
