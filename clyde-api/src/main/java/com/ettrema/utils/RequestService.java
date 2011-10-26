package com.ettrema.utils;

import com.bradmcevoy.http.Request;

/**
 * Determines if a request is secure
 *
 * @author brad
 */
public class RequestService {

    private final CurrentRequestService currentRequestService;

    public RequestService( CurrentRequestService currentRequestService ) {
        this.currentRequestService = currentRequestService;
    }

    /**
     * Is the current request secure
     *
     * @return
     */
    public boolean isSecure() {
        Request req = currentRequestService.request();
        return isSecure(req);
    }

    /**
     * Is the given request secure
     *
     * @param req
     * @return
     */
    public boolean isSecure(Request req) {
        if( req == null ) return false;
        String url = req.getAbsoluteUrl();
        if( url == null ) return false;
        return url.startsWith( "https" );
    }
}
