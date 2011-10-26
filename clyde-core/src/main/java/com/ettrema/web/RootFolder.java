package com.ettrema.web;

import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;


public class RootFolder extends Folder {
    
    private static final long serialVersionUID = 1L;
    
    public RootFolder(NameNode root) {
        super();
        this.nameNode = (RelationalNameNode) root.add("root",this);
    }
    
    public RootFolder() {
        super();
        this.nameNode = null;
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        throw new UnsupportedOperationException("Cant create root folder like this");
    }
    
    

    @Override
    public String getHref() {
        return "";
    }

    @Override
    public Folder getParent() {
        return null;
    }
    

}
