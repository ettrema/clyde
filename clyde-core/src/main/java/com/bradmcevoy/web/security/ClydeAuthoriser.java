package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;

/**
 * Interface for authorising access to pages and components
 *
 * @author brad
 */
public interface ClydeAuthoriser {

    /**
     * Used to identify this authoriser. Could just be the class name if typically
     * a singleton.
     * 
     * @return
     */
    String getName();

    /**
     * Authorise access to a page
     *
     * @param resource
     * @param request
     * @return - true means definitely does have access, false means defintely
     * do not. null means that this authoriser has no opinion.
     */
    Boolean authorise( Resource resource, Request request );
}
