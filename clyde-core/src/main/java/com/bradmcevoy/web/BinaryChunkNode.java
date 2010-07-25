package com.bradmcevoy.web;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.util.UUID;

public class BinaryChunkNode implements DataNode{
    
    private final byte[] data;
    
    UUID id;
    NameNode nameNode;
    
    public BinaryChunkNode(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
    
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
