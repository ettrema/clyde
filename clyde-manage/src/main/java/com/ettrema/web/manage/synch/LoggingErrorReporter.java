package com.ettrema.web.manage.synch;

import java.io.File;


/**
 *
 * @author brad
 */
public class LoggingErrorReporter implements ErrorReporter {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ErrorReporter.class );

    public void onError( File f, Exception ex ) {
        log.error("Exception processing: " + f.getAbsolutePath(), ex);
    }

}
