package com.bradmcevoy.utils;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;

/**
 *
 * @author brad
 */
public class DefaultRedirectService implements RedirectService{

    public String checkRedirect( Resource res, Request request ) {
        if( res instanceof BaseResource ) {
            BaseResource r = (BaseResource) res;
            String redirect = r.getRedirect();
            if( redirect != null && redirect.length() > 0 ) {
                return redirect;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
