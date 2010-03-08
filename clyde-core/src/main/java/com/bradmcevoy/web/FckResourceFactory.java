package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.vfs.NameNode;
import java.util.List;

public class FckResourceFactory extends AbstractClydeResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FckResourceFactory.class);

    public FckResourceFactory() {
    }

    @Override
    public Resource getResource(String host, String url) {
        host = this.resolveHostName(host);
        Path path = Path.path(url);
        if (FckFileManagerResource.URL.equals(path)) {
            Host h = getHost(host);
            if( h == null ) return null;
            FckFileManagerResource fck = new FckFileManagerResource(h);
            return fck;
        } else if (FckQuickUploaderResource.URL.equals(path)) {
            Host h = getHost(host);
            if( h == null ) return null;
            FckQuickUploaderResource fck = new FckQuickUploaderResource(h);
            return fck;
        } else {
            return null;
        }
    }
    
    protected Host getHost(String hostName) {
        List<NameNode> hosts = vfs().find(Host.class, hostName);
        if (hosts == null || hosts.size() == 0) {
            log.warn("host not found: " + hostName);
            return null;
        } else {
            if (hosts.size() > 1) {
                log.warn("found multiple hosts for: " + hostName + " - " + hosts.size());
            }
        }
        NameNode nnHost = hosts.get(0);
        Resource rHost = (Resource) nnHost.getData();
        if (rHost == null) {
            log.error("host is null: " + hostName);
            return null;
        } else if (rHost instanceof Host) {
            Host theHost = (Host) rHost;
            return theHost;
        } else {
            log.error("host is not a host type: " + hostName);
            return null;
        }
    }
        
}
