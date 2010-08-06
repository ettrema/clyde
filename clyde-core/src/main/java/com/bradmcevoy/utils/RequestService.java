package com.bradmcevoy.utils;

import com.bradmcevoy.http.Request;

/**
 *
 * @author brad
 */
public class RequestService {

    private final CurrentRequestService currentRequestService;

    public RequestService( CurrentRequestService currentRequestService ) {
        this.currentRequestService = currentRequestService;
    }

    public boolean isSecure() {
        Request req = currentRequestService.request();
        return isSecure(req);
    }

    public boolean isSecure(Request req) {
        if( req == null ) return false;
        String s = req.getAbsoluteUrl();
        if( s == null ) return false;
        return s.startsWith( "https" );
    }
}
