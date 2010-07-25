package com.bradmcevoy.web;

import com.bradmcevoy.http.FilterChain;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import com.ettrema.context.RootContextLocator;

/** Initialises the root context with the catalog file WEB-INF/catalog.xml
 *
 *  Opens a requestcontext for each request
 *
 *  Should be configued to load at ordinal 0
 */
public class ClydeFilter implements com.bradmcevoy.http.Filter  {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeFilter.class);
    
    final RootContextLocator rootContextLocator;

    public ClydeFilter( RootContextLocator rootContextLocator ) {
        this.rootContextLocator = rootContextLocator;
    }


    public ClydeFilter(RootContext rootContext) {
        this.rootContextLocator = new RootContextLocator();
        this.rootContextLocator.setRootContext( rootContext );
    }

    @Override
    public void process(final FilterChain chain, final Request request, final Response response) {
//        log.info(request.getMethod() + ": " + request.getAbsoluteUrl() + " START");
        long t = System.currentTimeMillis();
        RootContext rootContext = rootContextLocator.getRootContext();
        if( rootContext == null) {
            throw new IllegalStateException( "RootContext has not been set");
        }
        rootContext.execute( new Executable2() {
            @Override
            public void execute(Context context) {
                try {
                    chain.process( request, response );
                } catch( Exception e ) {
                    log.warn( "exception processing request", e);
                    throw new RuntimeException( e );
                }
            }
        });
//        t = System.currentTimeMillis() - t;
//        log.info(request.getMethod() + ": " + request.getAbsoluteUrl() + ": execute time: " + t + "ms : status: " + response.getStatus() );
    }
}
