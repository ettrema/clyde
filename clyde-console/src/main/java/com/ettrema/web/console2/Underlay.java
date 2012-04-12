package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.StreamUtils;
import com.ettrema.web.Folder;
import com.ettrema.console.Result;
import com.ettrema.underlay.UnderlayLocator;
import com.ettrema.underlay.UnderlayVector;
import com.ettrema.vfs.VfsTransactionManager;
import com.ettrema.web.Host;
import com.ettrema.web.manage.deploy.DeploymentService;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Underlay extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Underlay.class);
    private final UnderlayLocator underlayLocator;


    public Underlay(List<String> args, String host, String currentDir, ResourceFactory resourceFactory, DeploymentService deploymentService, UnderlayLocator underlayLocator) {
        super(args, host, currentDir, resourceFactory);
        this.underlayLocator = underlayLocator;
    }

    @Override
    public Result execute() {
        Folder cur;
        try {
            cur = currentResource();
        } catch (NotAuthorizedException | BadRequestException ex) {
            return result("can't lookup current resource", ex);
        }
        if (cur == null) {
            return result("current dir not found: " + currentDir);
        }
        String cmd = args.get(0);
        args.remove(0);
        switch( cmd ) {
            case "add":
                return doAdd(cur.getHost());
            case "remove":
                return doRemove(cur.getHost());
            default:
                return result("Unknown command: " + cmd);
        }

    }

    private Result doAdd(Host currentHost) {
        UnderlayVector v = new UnderlayVector();
        v.setGroupId(args.get(0));
        v.setArtifcatId(args.get(1));
        v.setVersion(args.get(2));
        List<UnderlayVector> vectors = currentHost.getUnderlayVectors();
        if( vectors == null ) {
            vectors = new ArrayList<>();
        }
        Iterator<UnderlayVector> it = vectors.iterator();
        while(it.hasNext()) {
            UnderlayVector oldV = it.next();
            //if( oldV.getGroupId().equals(v.getGroupId()))
        }
        return null;
    }

    private Result doRemove(Host currentHost) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
