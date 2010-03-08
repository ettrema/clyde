package com.bradmcevoy.event;

import com.bradmcevoy.web.BaseResource;

/**
 *
 * @author brad
 */
public interface ClydeResourceEvent extends Event {
    BaseResource getResource();
}
