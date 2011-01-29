package com.bradmcevoy.web.manage.synch;

import java.io.File;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author brad
 */
public class ErrorReporter {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ErrorReporter.class );

    private final JFrame frame;

    public ErrorReporter( JFrame frame ) {
        this.frame = frame;
    }
   

    void onError( File f, Exception ex ) {
        log.error("Exception processing: " + f.getAbsolutePath(), ex);
        JOptionPane.showMessageDialog( null, "Exception processing: " + f.getAbsolutePath() + " - " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
    }

}
