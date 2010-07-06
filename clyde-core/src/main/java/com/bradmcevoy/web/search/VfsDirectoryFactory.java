package com.bradmcevoy.web.search;

import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.Host;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import org.apache.lucene.store.Directory;

/**
 *
 */
public class VfsDirectoryFactory implements DirectoryFactory{

    public Directory open(String name) throws IOException {
        NameNode hostNode = node(name);
        if( hostNode == null ) {
            throw new RuntimeException("host doesnt not exist: " + name);
        }
        NameNode indexNode = hostNode.child("__index");
        if( indexNode == null ) {
            indexNode = hostNode.add("__index", new HostSearchDataNode());
            indexNode.save();
        }

        return new VfsDirectory(indexNode.getId());
    }

    public boolean exists(String name) {
        return true;
    }

    private VfsSession vfs() {
        return RequestContext.getCurrent().get(VfsSession.class);
    }

    private NameNode node(String name) {
        List<NameNode> nodes = vfs().find(Host.class, name);
        if( nodes == null || nodes.size() == 0 ) {
            return null;
        } else {
            return nodes.get(0);
        }
    }

    public static class HostSearchDataNode implements DataNode, Serializable {
        private static final long serialVersionUID = 1L;
        private UUID id;

        public void setId(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }

        public void init(NameNode nameNode) {

        }

        public void onDeleted(NameNode nameNode) {
            
        }

    }

}
