package com.ettrema.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.SourcePage;

/**
 * Grants permission to operate of the source if the user has permission to delete
 * the physical resource.
 *
 *
 * @author brad
 */
public class SourcePageAuthoriser implements ClydeAuthoriser{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SourcePageAuthoriser.class);

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method, Auth auth ) {
        if(resource instanceof SourcePage) {
            SourcePage sp = (SourcePage) resource;
            return sp.res.authorise( request, Request.Method.DELETE, auth);
        } else {
            return null;
        }
    }
}
