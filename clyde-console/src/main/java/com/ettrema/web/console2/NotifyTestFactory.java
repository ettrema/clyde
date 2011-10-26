package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.berry.event.Notifier;
import com.ettrema.console.ConsoleCommand;
import java.util.List;

public class NotifyTestFactory extends AbstractFactory {

    private final Notifier notifier;

    public NotifyTestFactory( Notifier notifier ) {
        super( "Send a test message using the Notifier server. Eg notify 1 somecat aMessage", new String[]{"notify"} );
        this.notifier = notifier;
    }

    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new NotifyTest( args, host, currentDir, resourceFactory, notifier );
    }
}
