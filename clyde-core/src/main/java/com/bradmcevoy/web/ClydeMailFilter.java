package com.bradmcevoy.web;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Executable2;
import com.bradmcevoy.context.Factory;
import com.bradmcevoy.context.Registration;
import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.context.RootContextLocator;
import com.ettrema.mail.Event;
import com.ettrema.mail.Filter;
import com.ettrema.mail.FilterChain;

/**
 *  This implements both factory and filter, in that as a factory it just
 * returns itself as the filter.
 *
 * @author brad
 */
public class ClydeMailFilter implements com.ettrema.mail.Filter, Factory<Filter> {

    private RootContextLocator contextLocator;
    public static Class[] classes = {Filter.class};

    @Override
    public void doEvent( final FilterChain chain, final Event event ) {
        rootContext().execute( new Executable2() {

            @Override
            public void execute( Context context ) {
                chain.doEvent( event );
            }
        } );
    }

    RootContext rootContext() {
        return contextLocator.getRootContext();
    }

    @Override
    public Class[] keyClasses() {
        return classes;
    }

    @Override
    public String[] keyIds() {
        return null;
    }

    @Override
    public Registration<Filter> insert( RootContext context, Context requestContext ) {
        throw new UnsupportedOperationException( "Should never happen, because already inserted in init" );
    }

    @Override
    public void init( RootContext context ) {
        this.contextLocator = new RootContextLocator();
        contextLocator.setRootContext( context );
        context.put( this );
    }

    public void setContextLocator( RootContextLocator contextLocator ) {
        this.contextLocator = contextLocator;
    }

    

    @Override
    public void destroy() {
    }

    @Override
    public void onRemove( Filter item ) {
    }
}
