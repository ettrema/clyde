package com.ettrema.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;

/**
 * Very simple implementation of CachePolicyService, returns a constant
 * value for all resource types
 *
 * @author brad
 */
public class DefaultCachePolicyService implements CachePolicyService{

    private long maxAgeSeconds;

    public Long getMaxAgeSeconds( Resource r, Auth auth ) {
        return maxAgeSeconds;
    }

    public long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds( long maxAgeSeconds ) {
        this.maxAgeSeconds = maxAgeSeconds;
    }



}
