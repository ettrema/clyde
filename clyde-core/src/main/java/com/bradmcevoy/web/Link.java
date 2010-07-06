package com.bradmcevoy.web;

import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.Relationship;

/**
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
