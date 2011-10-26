package com.ettrema.utils;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public class DefaultRedirectService implements RedirectService{

	@Override
    public String checkRedirect( Resource res, Request request ) {
        if( res instanceof Redirectable ) {
            Redirectable r = (Redirectable) res;
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
