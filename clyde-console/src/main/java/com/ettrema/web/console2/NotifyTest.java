package com.ettrema.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.berry.event.Notifier;
import com.ettrema.console.Result;
import java.util.List;

public class NotifyTest extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( NotifyTest.class );

    private final Notifier notifier;

    NotifyTest( List<String> args, String host, String currentDir, ResourceFactory resourceFactory, Notifier notifier ) {
        super( args, host, currentDir, resourceFactory );
        this.notifier = notifier;
    }

    public Result execute() {
        if( args.size() != 3 ) {
            return result( "incorrect number of arguments, expected 3: urgency cat msg");
        } else {
            int urgency = Integer.parseInt( args.get(0));
            String cat = args.get(1);
            String msg = args.get(2);
            notifier.notify( urgency, cat, msg);
            return result( "Sent");
        }
    }
}
