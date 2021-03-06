package com.ettrema.binary;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.UUID;

/**
 *
 */
public class Versions implements DataNode, Serializable{

    private static final long serialVersionUID = 1L;

    private UUID id;

    private transient NameNode nameNode;

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void init(NameNode nameNode) {
        this.nameNode = nameNode;
    }

    public void onDeleted(NameNode nameNode) {
        
    }

}
