package com.ettrema.utils;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;

/**
 *
 * @author brad
 */
public class CurrentRequestService {
    public Request request() {
        return HttpManager.request();
    }

}
