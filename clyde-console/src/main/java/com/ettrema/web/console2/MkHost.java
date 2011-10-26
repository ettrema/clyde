
package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.ettrema.console.Result;
import java.util.List;

public class MkHost extends AbstractConsoleCommand {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MkHost.class);

    public Folder newCol;

    MkHost(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    protected Host doCreate(Folder parent, String newName) {
        Host newHost = new Host(parent, newName);
        newHost.save();
        log.debug( "created new host: " + newHost.getPath() + " in parent: " + parent.getHref());
        return newHost;
    }

    @Override
    public Result execute() {
        String newName = args.get(0);
        log.debug("mkdir. execute: " + newName);
        Folder cur = currentResource();
        if (cur == null) {
            log.debug("current resource not found: " + currentDir);
            return result("current dir not found: " + currentDir);
        } else {
            Result validationResult = validate(cur,newName);
            if( validationResult != null ) {
                return validationResult;
            }
            newCol = doCreate(cur, newName);
            commit();
            return new Result(currentDir, "created: <a href='" + newCol.getHref() + "'>" + newCol.getName() + "</a>");
        }
    }

    protected Result validate(Folder cur, String newName) {
        Resource existing = cur.child(newName);
        if( existing != null ) {
            return result("An item of that name already exists. Is a: " + existing.getClass());
        } else {
            return null;
        }
    }
}
