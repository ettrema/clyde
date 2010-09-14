package com.bradmcevoy.utils;

import com.ettrema.common.Service;
import java.io.File;

/**
 *
 * @author brad
 */
public class LogMonitorService implements Service {

    private LogMonitor logMonitor;
    private Thread thread;
    private final File logFile;
    private final long interval;

    public LogMonitorService( File logFile, long interval ) {
        this.logFile = logFile;
        this.interval = interval;
        if( !logFile.exists() ) {
            throw new RuntimeException( "Log file does not exist: " + logFile.getAbsolutePath());
        }
    }

    public void start() {
        logMonitor = new LogMonitor( interval, logFile );
        thread = new Thread( logMonitor );
        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }
}
