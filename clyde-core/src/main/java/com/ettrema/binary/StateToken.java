package com.ettrema.binary;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class StateToken implements DataNode, Serializable {

    private static final long serialVersionUID = 1L;
    
    private UUID id;
    
    private long crc;
    
    private transient NameNode nameNode;

    public long getCrc() {
        return crc;
    }

    public void setCrc(long crc) {
        this.crc = crc;
        nameNode.save();
    }
    
    
    
    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void init(NameNode nameNode) {
        this.nameNode = nameNode;
    }

    @Override
    public void onDeleted(NameNode nameNode) {
        
    }

    void delete() {
        nameNode.delete();
    }
    
}
