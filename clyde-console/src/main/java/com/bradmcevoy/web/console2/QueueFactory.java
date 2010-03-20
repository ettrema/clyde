package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import com.bradmcevoy.grid.QueueManager;
import java.util.List;

/**
 *
 * @author brad
 */
public class QueueFactory extends AbstractFactory {

    private final QueueManager queueManager;

    public QueueFactory( QueueManager queueManager ) {
        super( "Report on the queue length. Eg queue", new String[]{"queue"} );
        this.queueManager = queueManager;
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Queue( args, host, currentDir, resourceFactory, queueManager );
    }
}
