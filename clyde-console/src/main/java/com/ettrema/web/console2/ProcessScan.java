package com.ettrema.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.process.ProcessDef;
import com.ettrema.web.Folder;
import com.ettrema.console.Result;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.VfsSession;
import java.util.List;

/**
 *
 * @author brad
 */
public class ProcessScan  extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Migrate.class );


    ProcessScan( List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super( args, host, currentDir, resourceFactory );
    }

    @Override
    public Result execute() {
        VfsSession sess = RequestContext.getCurrent().get( VfsSession.class );

        Folder cur = this.currentResource();
        log.info("do process scan: " + cur.getPath());

        boolean didSomething = ProcessDef.scan(cur);

        sess.commit();

        return result("did something? " + didSomething);
    }
}
