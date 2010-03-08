
package com.bradmcevoy.web.search;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.grid.Processable;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Template;
import java.io.Serializable;
import java.util.UUID;

public class BaseResourceIndexer implements Processable, Serializable{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BaseResourceIndexer.class);

    private static final long serialVersionUID = 1L;

    private final UUID nodeId;

    public BaseResourceIndexer(UUID nodeId) {
        this.nodeId = nodeId;
    }
    
    
    @Override
    public void doProcess(Context context) {
//        log.debug("doProcess: " + nodeId);
        if( context == null ) throw new NullPointerException("No context");
        VfsSession vfs = context.get(VfsSession.class);
        NameNode node = vfs.get(nodeId);
        if( node == null ) {
            log.warn("Name node not found: " + nodeId);
            return ;
        }
        DataNode dn = node.getData();
        if( dn == null ) {
            log.warn("No data node associated with name node: " + nodeId);
            return ;
        }
        if( dn instanceof Template ) {
            return ;
        } else if( dn instanceof Folder ) {
            return ;            
        } else if( dn instanceof BaseResource ) {
            BaseResource res = (BaseResource) dn;
            if( res.isTrash()) {
                log.debug( "not indexing as in trash: " + res.getPath());
            } else {
                BaseResourceIndexer.process(res);
            }
        } else {
            log.debug("Datanode is not of type BaseResource. Data node id: " + dn.getId() + ". Name node id: " + this.nodeId + ". Is type: " + dn.getClass());
        }
    }

    
    private synchronized static void process(BaseResource res) {
//        log.debug("indexing: " + res.getPath());
        Host host = res.getHost();
        if( host == null ) {
            log.warn("No host for: " + res.getHref());
            return ;
        }
        String hostName = host.getName();
        HostSearchManager mgr = HostSearchManager.getInstance(hostName);
        mgr.index(res);
    }

    @Override
    public void pleaseImplementSerializable() {
    }
    

    
}
