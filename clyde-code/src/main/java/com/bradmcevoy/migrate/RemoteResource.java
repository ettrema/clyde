package com.bradmcevoy.migrate;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.web.BaseResource;
import java.util.Date;

public class RemoteResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RemoteResource.class);

    Path destFolder;
    BaseResource res;
    private final com.ettrema.httpclient.Host remoteHost;
    private final MigrationHelper migrationHelper;
    
    public RemoteResource(Path destFolder, BaseResource res, com.ettrema.httpclient.Host remoteHost, MigrationHelper migrationHelper) {
        this.remoteHost = remoteHost;
        this.migrationHelper = migrationHelper;
        this.destFolder = destFolder;
        this.res = res;
        if (res == null) {
            throw new IllegalArgumentException("local resource cannot be null");
        }
    }

    /**
     *
     * @return - modified date
     */
    Date getModifiedDate() throws Exception {        
        String path = getRemotePath().toString();
        System.out.println("getModifiedDate: destFolder: " + destFolder + " name: " + res.getName() + " = " + path);
        com.ettrema.httpclient.Resource r = remoteHost.find(path);
        if (r == null) {
            log.trace("getModifiedDate: not found: " + path);
            return null;
        }
        return r.getModifiedDate();
    }

    /**
     * Creates or updates the remote resource
     *
     * @throws Exception
     */
    public void doPut() throws Exception {
        log.warn("put: " + this.getRemotePath());
        com.ettrema.httpclient.Folder parent = remoteHost.getOrCreateFolder(destFolder, true);
        if( parent == null ) {
            throw new RuntimeException("Failed to get parent: " + destFolder + " from remote host: " + remoteHost.href());
        }
        com.ettrema.httpclient.Resource remote = parent.child(res.getName());
        try {
            if (remote != null && !(remote instanceof com.ettrema.httpclient.Folder)) {
                log.warn(" - delete existing remote resource: " + remote.name);
                remote.delete();
            }
        } catch (Exception e) {
            throw new Exception("Couldnt delete: " + remote.href());
        }
        try {
            migrationHelper.updateContentAndMeta(remoteHost, this.destFolder, this.res);
        } catch (Exception ex) {
            throw new Exception("sourceUri:" + res.getHref(), ex);
        }
    }

    public Path getRemotePath() {
        if( destFolder == null ) {
            return Path.path(res.getName());
        } else {
            return destFolder.child(res.getName());
        }
    }
}
