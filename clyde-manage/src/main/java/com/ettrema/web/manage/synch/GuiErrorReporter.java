package com.ettrema.web.manage.synch;

import java.io.File;
import javax.swing.JOptionPane;


/**
 *
 * @author brad
 */
public class GuiErrorReporter implements ErrorReporter {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ErrorReporter.class );

    public void onError( File f, Exception ex ) {
        log.error(f.getAbsolutePath(), ex);
        JOptionPane.showMessageDialog( null, "Exception processing: " + f.getAbsolutePath() + " - " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    }
}
