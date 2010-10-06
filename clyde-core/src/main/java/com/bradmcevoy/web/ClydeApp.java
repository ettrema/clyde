package com.bradmcevoy.web;

import com.bradmcevoy.http.EventListener;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.web.stats.StatsFilter;
import com.ettrema.context.RootContext;
import java.util.Map;

/**
 * Just sets up the clyde transactional filter and stats filters on
 * the milton httpmanager
 *
 * And sets request params into thread locals on get and post
 *
 * @author brad
 */
public class ClydeApp implements EventListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeApp.class);

    public final RootContext rootContext;

    public ClydeApp( RootContext rootContext, HttpManager manager ) {
        this(rootContext, manager, true );
    }

    public ClydeApp( RootContext rootContext, HttpManager manager, boolean enableStats ) {
        log.warn( "Hello from ClydeApp");
        this.rootContext = rootContext;

        ClydeFilter clydeFilter = new ClydeFilter( rootContext );
        rootContext.put( clydeFilter );

        if( enableStats ) {
            log.warn("Adding stats filter");
            StatsFilter statsFilter = new StatsFilter( rootContext );
            statsFilter.init();
            manager.addFilter( 0, statsFilter );
        } else {
            log.warn("not adding stats filter");
        }
        
        manager.addFilter( 0, clydeFilter );

        log.info("adding myself as event listener to setup RequestParams for GET and POST requests");
        manager.addEventListener( this );
    }

    @Override
    public void onProcessResourceStart( Request request, Response response, Resource resource ) {
    }

    @Override
    public void onGet( Request request, Response response, Resource resource, Map<String, String> params ) {
        RequestParams.setCurrent( new RequestParams( resource, request, params, null ) );
    }

    @Override
    public void onPost( Request request, Response response, Resource resource, Map<String, String> params, Map<String, FileItem> files ) {
        RequestParams.setCurrent( new RequestParams( resource, request, params, files ) );
    }

    @Override
    public void onProcessResourceFinish( Request request, Response response, Resource resource, long duration ) {
        RequestParams.setCurrent( null );
    }
}
