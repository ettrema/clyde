package com.ettrema.utils;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;

/**
 * Implementations determine if a redirect is needed.
 *
 * This can be from a folder to its index page, or from http to https for
 * secure sites
 *
 * @author brad
 */
public interface RedirectService {
    String checkRedirect( Resource ct, Request request );
}
