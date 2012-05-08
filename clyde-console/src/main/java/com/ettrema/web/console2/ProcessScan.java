package com.ettrema.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.process.ProcessDef;
import com.ettrema.web.Folder;
import com.ettrema.console.Result;
import com.ettrema.vfs.VfsTransactionManager;
import java.util.List;

/**
 *
 * @author brad
 */
public class ProcessScan extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Migrate.class);

    ProcessScan(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    @Override
    public Result execute() {
        Folder cur;
        try {
            cur = currentResource();
        } catch (NotAuthorizedException | BadRequestException ex) {
            return result("can't lookup current resource", ex);
        }
        log.info("do process scan: " + cur.getPath());

        boolean didSomething = ProcessDef.scan(cur);

        VfsTransactionManager.commit();

        return result("did something? " + didSomething);
    }
}
