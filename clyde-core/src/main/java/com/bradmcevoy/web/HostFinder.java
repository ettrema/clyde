package com.bradmcevoy.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.vfs.VfsCommon;
import com.ettrema.vfs.NameNode;
import java.util.List;

/**
 *
 * @author brad
 */
public class HostFinder extends VfsCommon {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( HostFinder.class );

    public Host getHost( String hostName ) {
        if( hostName == null ) {
            log.debug( "getHost with null name" );
            return null;
        }
        List<NameNode> hosts = vfs().find( Host.class, hostName );
        if( hosts == null || hosts.size() == 0 ) {
            log.warn( "host not found: " + hostName );
            return null;
        } else {
            if( hosts.size() > 1 ) {
                log.warn( "found multiple hosts for: hostname=" + hostName + " - number found=" + hosts.size() );
            }
        }
        NameNode nnHost = hosts.get( 0 );
        Resource rHost = (Resource) nnHost.getData();
        if( rHost == null ) {
            log.error( "host is null: " + hostName );
            return null;
        } else if( rHost instanceof Host ) {
            Host theHost = (Host) rHost;
            return theHost;
        } else {
            log.error( "host is not a host type: " + hostName );
            return null;
        }
    }
}
