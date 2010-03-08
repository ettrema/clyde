package com.bradmcevoy.web;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Executable2;
import com.bradmcevoy.context.RootContext;
import com.ettrema.ftp.FtpActionListener;
import java.util.concurrent.Callable;

/**
 *
 * @author brad
 */
public class TransactionalFtpFilter implements FtpActionListener{

    private final RootContext rootContext;

    public TransactionalFtpFilter( RootContext rootContext ) {
        this.rootContext = rootContext;
    }

    @Override
    public void onAction( final Runnable r ) {
        rootContext.execute( new Executable2() {

            @Override
            public void execute( Context context ) {
                r.run();
            }
        });
    }

    @Override
    public <V> V onAction( Callable<V> arg0 ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
