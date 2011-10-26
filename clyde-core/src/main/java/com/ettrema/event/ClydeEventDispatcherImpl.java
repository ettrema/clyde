package com.ettrema.event;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.RenderContext;

/**
 * TODO: execute groovy scripts on templated resource
 *
 * @author brad
 */
public class ClydeEventDispatcherImpl implements ClydeEventDispatcher {

    public void beforeRender( CommonTemplated ct, RenderContext child ) {

    }

    public void beforeGet( Resource r, Request request, Response response ) {

    }

    public void beforeDelete( Resource r, Request request, Response response ) {

    }

    public String onFormSubmit( Resource r, Request request, Response response ) {
        return null;
    }

}
