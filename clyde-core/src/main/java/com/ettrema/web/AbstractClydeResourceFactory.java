
package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.vfs.VfsCommon;
import com.ettrema.vfs.NameNode;
import java.util.List;

public abstract class AbstractClydeResourceFactory extends VfsCommon implements ResourceFactory {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractClydeResourceFactory.class);
    
    public static final ThreadLocal<Host> tlAliasedHost = new ThreadLocal<Host>();
    
    protected String resolveHostName(String host) {
        host = stripPort(host);
        tlAliasedHost.set(null);
        List<NameNode> hosts = vfs().find(Host.class,host);
        
        if( hosts == null || hosts.isEmpty() ) {
            hosts = vfs().find(Host.class,"www." + host); // we accept host.com as well as www.host.com
            if( hosts == null || hosts.isEmpty() ) {
                log.warn("host not found: " + host);
                return null;
            }
        } else {
            if( hosts.size() > 1 ) {
                log.warn("found multiple hosts for: " + host + " - " + hosts.size());
            }
        }
        NameNode nnHost = hosts.get(0);
        Resource rHost = (Resource) nnHost.getData();

        if( rHost == null ) {
            log.error("host is null: " + host);
            return null;
        } else if( rHost instanceof Host ) {
            Host theActualHost = (Host) rHost;
            if( theActualHost.isAlias() ) {
                Path hostPath = theActualHost.hostPath;
                Host aliasedHost = theActualHost.getAliasedHost();
                if( aliasedHost == null ) {
                    log.warn("Aliased host not found: " + hostPath);
                    return null;
                } else {
                    System.setProperty("simulate.server", "true");  // TODO: fixme: see PageImageRenderer
                    tlAliasedHost.set(theActualHost);
                    return aliasedHost.getName();
                }
            } else {
                return theActualHost.getName();
            }
        } else {
            log.error("host is not a host type: " + host);
            return null;
        }                
    }
    
    private String stripPort(final String host) {
        if( host == null ) return "";
        String h = host;
        if( h.contains(":")) {
            String[] arr = h.split(":");
            h = arr[0];
        }
        return h;
    }
}
