package com.bradmcevoy.web.creation;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.User;
import com.ettrema.vfs.Relationship;
import java.util.List;

/**
 *
 * @author brad
 */
public class RelationshipCreatorService implements CreatorService {

    public User getCreator( BaseResource res ) {
        List<Relationship> rels = res.getNameNode().findFromRelations( "creator" );
        if( rels == null || rels.size() == 0 ) return null;
        User creator = (User) rels.get( 0 ).to().getData();
        return creator;
    }

    public void setCreator( User user, BaseResource res ) {
        res.getNameNode().makeRelation( user.getNameNode(), "creator" );
    }
}
