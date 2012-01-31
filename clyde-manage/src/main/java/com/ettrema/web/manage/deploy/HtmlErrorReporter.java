package com.ettrema.web.manage.deploy;

import com.ettrema.web.manage.synch.ErrorReporter;
import java.io.File;

/**
 *
 * @author brad
 */
public class HtmlErrorReporter implements ErrorReporter {

    @Override
    public void onError(File f, Exception ex) {
        // TODO: create html
    }
    
}
