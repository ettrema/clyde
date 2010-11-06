package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
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
     * @param method - the method to test for. This should be used in preference to that on the request
     * @return - true means definitely does have access, false means defintely
     * do not. null means that this authoriser has no opinion.
     */
    Boolean authorise( Resource resource, Request request, Method method, Auth auth );
}
