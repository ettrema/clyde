package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.console.Result;
import com.ettrema.grid.aws.QueueManager;
import java.util.List;

/**
 *
 * @author brad
 */
public class Queue extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Recreate.class );

    private final QueueManager queueManager;

    Queue( List<String> args, String host, String currentDir, ResourceFactory resourceFactory, QueueManager queueManager ) {
        super( args, host, currentDir, resourceFactory );
        this.queueManager = queueManager;
    }

    @Override
    public Result execute() {
        int queueSize = queueManager.getQueueSize();
        log.debug( "queue size: " + queueSize);
        return result( "queue size: " + queueSize);
    }

}
