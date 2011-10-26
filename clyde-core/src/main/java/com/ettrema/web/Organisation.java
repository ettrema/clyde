package com.ettrema.web;

public class Organisation extends Host{
    
    private static final long serialVersionUID = 1L;
    
    public Organisation(Folder parent, String name) {
        super(parent,name);	
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new Organisation(parent, newName);
    }
    
    
}
