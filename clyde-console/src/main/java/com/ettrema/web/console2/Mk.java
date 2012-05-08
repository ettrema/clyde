
package com.ettrema.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.utils.ReflectionUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.ITemplate;
import com.ettrema.console.Result;
import java.util.List;

public class Mk extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Mk.class);

    Mk(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    protected Result doCreate(Folder parent, String newName, String sClazz) {
        BaseResource res;
        ITemplate t = parent.getTemplate(sClazz);
        if( t != null ) {
            res = t.createPageFromTemplate(parent, newName);
        } else {
            res = (BaseResource) ReflectionUtils.create(sClazz, parent, newName);
        }
        res.save();
        commit();
        return result("Created: " + res.getLink());

    }

    @Override
    public Result execute() {
        String sClazz = args.get(0);
        String newName = args.get(1);
        log.debug("mk. execute: " + sClazz + " - " + newName);
        Folder cur;
        try {
            cur = currentResource();
        } catch (NotAuthorizedException | BadRequestException ex) {
            return result("can't lookup current resource", ex);
        }

        if (cur == null) {
            log.debug("current resource not found: " + currentDir);
            return result("current dir not found: " + currentDir);
        } else {
            Result validationResult = validate(cur,newName);
            if( validationResult != null ) {
                return validationResult;
            }
            Result result = doCreate(cur, newName, sClazz);
            commit();
            return result;
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
