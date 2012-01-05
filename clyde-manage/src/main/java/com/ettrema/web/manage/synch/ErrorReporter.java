package com.ettrema.web.manage.synch;

import java.io.File;

/**
 *
 * @author brad
 */
public interface  ErrorReporter {
    
    void onError( File f, Exception ex );

}
