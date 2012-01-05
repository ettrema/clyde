package com.ettrema.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import java.util.List;

/**
 *
 * @author brad
 */
public class MultipleAuthoriser implements ClydeAuthoriser{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultipleAuthoriser.class);

    private final List<ClydeAuthoriser> authorisers;

    public MultipleAuthoriser( List<ClydeAuthoriser> authorisers ) {
        this.authorisers = authorisers;
    }



    @Override
    public Boolean authorise( Resource resource, Request request, Method method, Auth auth ) {
        log.trace("authorise");
        for( ClydeAuthoriser a : authorisers ) {
            if(log.isTraceEnabled()) {
                log.trace("check: " + a.getClass());
            }
            Boolean res = a.authorise( resource, request, method, auth );
            if( res != null) {
                log.debug( "authoriser said: " + res + " - " + a.getName());
                return res.booleanValue();
            }
        }
        log.debug( "no authoriser expressed an opinion: " + resource.getName() );
        return true;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

}
