package com.bradmcevoy.web;

import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.ettrema.vfs.NameNode;

public abstract class File extends BaseResource implements CopyableResource, DeletableResource, GetableResource, MoveableResource, HtmlResource{
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(File.class);
    
    private static final long serialVersionUID = 1L;
    
    public File(String contentType, Folder parentFolder, String newName) {
        super(contentType,parentFolder,newName);
    }

    @Override
    public boolean is( String type ) {
        if( "file".equals( type)) return true;
        return super.is( type );
    }



    @Override
    public void onDeleted(NameNode nameNode) {
    }
    
}
