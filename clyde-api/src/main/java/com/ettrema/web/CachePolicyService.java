package com.ettrema.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;

/**
 * Implementations provide a means for determining how long a resource should
 * be cached in browsers.
 *
 * This may differ for different resource types. For example html pages will
 * often not be cached at all so that chnanged data is immediately displayed,
 * while images will often be cached for long periods of time to ensure they
 * don't get downloaded more then is required
 *
 * @author brad
 */
public interface CachePolicyService {
    /**
     *
     * @param r - the resource being requested
     * @param auth - represents the currently logged in user
     * @return - null to indicate no caching, or else the number of seconds the
     * resource should remain cached for
     */
    public Long getMaxAgeSeconds( Resource r, Auth auth );
}
