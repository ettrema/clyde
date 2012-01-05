package com.ettrema.web;

import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.Relationship;


/**
 * I don't think this is used, might not be any use.. ?
 *
 * @author brad
 */
public class Link extends BaseResource{

    private static final long serialVersionUID = 1L;
    
    public Link( Folder parentFolder, String newName ) {
        super( null, parentFolder, newName );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new Link( parent, newName );
    }

    @Override
    public String getDefaultContentType() {
        BaseResource linked = getDest();
        return linked.getDefaultContentType();
    }

    @Override
    public void onDeleted( NameNode nameNode ) {
        
    }

    public void setDest(BaseResource res) {
        // first remove any previous "linked" relationship
        for( Relationship rel : this.getNameNode().findFromRelations( "linked") ) {
            rel.delete();
        }
        this.getNameNode().makeRelation( res.getNameNode(), "linked");
    }

    public BaseResource getDest() {
        for( Relationship rel : this.getNameNode().findFromRelations( "linked") ) {
            return (BaseResource) rel.to().getData();
        }
        return null;
    }

    @Override
    public boolean isIndexable() {
        return false;
    }

}
