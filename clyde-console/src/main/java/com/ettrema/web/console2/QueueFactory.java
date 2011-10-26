package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.common.Service;
import com.ettrema.console.ConsoleCommand;
import com.ettrema.grid.QueueManager;
import java.util.List;

/**
 *
 * @author brad
 */
public class QueueFactory extends AbstractFactory {

    private final QueueManager queueManager;
    private Service queueProcesor;
    private Service asyncProcessor;

    public QueueFactory( QueueManager queueManager ) {
        super( "Report on the queue length. Options: -query [num] -purge [num] [type] Eg queue, or queue -purge 100 com.bradmcevoy.Job, or queue -query 10: other options, -pauseQ, -resumeQ, -pauseAsync, -resumeAsync", new String[]{"queue"} );
        this.queueManager = queueManager;
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Queue( args, host, currentDir, resourceFactory, queueManager, queueProcesor, asyncProcessor );
    }

    public Service getQueueProcesor() {
        return queueProcesor;
    }

    public void setQueueProcesor( Service queueProcesor ) {
        this.queueProcesor = queueProcesor;
    }

    public Service getAsyncProcessor() {
        return asyncProcessor;
    }

    public void setAsyncProcessor( Service asyncProcessor ) {
        this.asyncProcessor = asyncProcessor;
    }
}
