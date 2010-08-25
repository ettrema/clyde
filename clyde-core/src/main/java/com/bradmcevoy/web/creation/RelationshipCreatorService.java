package com.bradmcevoy.web.creation;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.User;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.Relationship;
import java.util.List;

/**
 *
 * @author brad
 */
public class RelationshipCreatorService implements CreatorService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RelationshipCreatorService.class );

    public User getCreator( BaseResource res ) {
        List<Relationship> rels = res.getNameNode().findFromRelations( "creator" );
        if( rels == null || rels.size() == 0 ) {
            return null;
        } else {
            NameNode nTo = rels.get( 0 ).to();
            if( nTo == null ) {
                log.warn("found relationship, but to node is null");
                return null;
            } else {
                DataNode data = nTo.getData();
                if( data == null ) {
                    log.debug( "found to node, but data is null");
                    return null;
                } else if( data instanceof User ) {
                    return (User) data;
                } else {
                    log.debug( "found to node, but data is not a user. Is a: " + data.getClass().getCanonicalName());
                    return null;
                }
            }
        }
    }

    public void setCreator( User user, BaseResource res ) {
        res.getNameNode().makeRelation( user.getNameNode(), "creator" );
    }
}