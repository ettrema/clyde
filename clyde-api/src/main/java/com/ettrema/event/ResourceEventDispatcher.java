package com.ettrema.event;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;


/**
 *
 * @author brad
 */
public interface ResourceEventDispatcher {
    void beforeGet(Resource r, Request request, Response response);

    void beforeDelete(Resource r, Request request, Response response);

    String onFormSubmit(Resource r, Request request, Response response);
}
