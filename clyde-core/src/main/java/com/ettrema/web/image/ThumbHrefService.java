package com.bradmcevoy.web.image;

import com.bradmcevoy.http.Resource;

/**
 * Simple interface for finding the thumb url for a resource.
 *
 * @author brad
 */
public interface ThumbHrefService {
    /**
     * Return the path (NOT a full URL) from the resource's host to the resource;s
     * thumb image.
     *
     * Returns null if there is no thumb, or if the resource is not suitable for
     * thumbs
     *
     * @param r
     * @param - an identifier for the resolution of the thumb -eg _sys_thumb
     * @return - null if no thumb, otherwise a path from the resource's host to
     * its thumb if one exists
     */
    String getThumbPath(Resource r, String suffix);
}
