package com.bradmcevoy.web.manage.synch;

import com.bradmcevoy.web.code.CodeResourceFactory;
import com.ettrema.common.Service;
import com.ettrema.context.RootContext;
import java.io.File;
import javax.swing.JFrame;

/**
 *
 * @author brad
 */
public class FileManager implements Service{


    private final RootContext rootContext;
    private final File webapp;

    private final ErrorReporter errorReporter;

    private final FileWatcher fileWatcher;

    private final FileLoader fileLoader;

    public FileManager(RootContext rootContext, JFrame frame, File webapp, CodeResourceFactory resourceFactory) {
        this.rootContext = rootContext;
        this.webapp = webapp;
        errorReporter = new ErrorReporter(frame);
        this.fileLoader = new FileLoader(webapp, resourceFactory, errorReporter);
        this.fileWatcher = new FileWatcher( rootContext, webapp, fileLoader );
    }



    public void start() {
        fileWatcher.initialScan();
        fileWatcher.start();
    }

    public void stop() {
        
    }
}
