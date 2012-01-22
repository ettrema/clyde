
package com.ettrema.web.search;

import com.ettrema.web.BaseResource;
import com.ettrema.web.Host;
import com.ettrema.context.Context;
import com.ettrema.context.RequestContext;
import com.ettrema.grid.Processable;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.io.Serializable;
import java.util.UUID;
import org.apache.lucene.index.CorruptIndexException;

public class BaseResourceIndexer implements Processable, Serializable{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BaseResourceIndexer.class);

    private static final long serialVersionUID = 1L;

    private final UUID nodeId;

    public BaseResourceIndexer(UUID nodeId) {
        this.nodeId = nodeId;
    }
    
    
    @Override
    public void doProcess(Context context) {
        log.debug("doProcess: " + nodeId + " -------------------------");
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
        } else if( dn instanceof BaseResource ) {
            BaseResource res = (BaseResource) dn;
            if( res.isTrash()) {
                log.debug( "not indexing as in trash: " + res.getPath());
            } else {
                log.debug("res: " + res.getClass().getCanonicalName());
                if( res.isIndexable() ) {
                    BaseResourceIndexer.process(res);
                    log.debug("doProcess: done -------------------------");
                } else {
                    log.debug("not indexable");
                }
            }
        } else {
            log.debug("Datanode is not of type BaseResource. Data node id: " + dn.getId() + ". Name node id: " + this.nodeId + ". Is type: " + dn.getClass());
        }
    }

    
    private synchronized static void process(BaseResource res) {
        log.debug("indexing: " + res.getHref());
        Host host = res.getHost();
        if( host == null ) {
            log.warn("No host for: " + res.getHref());
            return ;
        }
        String hostName = host.getName();
        SearchManager sm = RequestContext.getCurrent().get(SearchManager.class);        
		if( sm == null ) {
			log.warn("No " + SearchManager.class + " in context, so cant' do search indexing");
			return ;
		}
        try {
            sm.index(res);
        } catch (CorruptIndexException ex) {
            log.error("couldnt index in: " + hostName, ex);
        }
    }

    @Override
    public void pleaseImplementSerializable() {
    }
    

    
}
