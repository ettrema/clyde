package com.bradmcevoy.utils;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import java.util.List;

/**
 *
 * @author brad
 */
public class MultiRedirectService implements RedirectService {
    private final List<RedirectService> redirectServices;

    public MultiRedirectService( List<RedirectService> redirectServices ) {
        this.redirectServices = redirectServices;
    }

	@Override
    public String checkRedirect( Resource ct, Request request ) {
        for( RedirectService svc : redirectServices) {
            String s = svc.checkRedirect( ct, request );
            if( s != null ) {
				System.out.println("multi redirect, going to: " + s + " because of: " + svc.getClass());
                return s;
            }
        }
        return null;
    }


}
