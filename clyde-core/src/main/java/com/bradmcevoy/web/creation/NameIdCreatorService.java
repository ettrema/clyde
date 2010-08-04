package com.bradmcevoy.web.creation;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.User;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.UUID;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class NameIdCreatorService implements CreatorService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( NameIdCreatorService.class );

    private final CreatorService wrapped;

    public NameIdCreatorService( CreatorService wrapped ) {
        this.wrapped = wrapped;
    }
                
    public User getCreator( BaseResource res ) {
        UUID creatorId = res.getCreatorNameNodeId();
        if( creatorId == null ) {
            return wrapped.getCreator( res );
        } else {
            NameNode nn = _(VfsSession.class).get( creatorId );
            if( nn == null ) {
                log.warn( "user name node not found: " + creatorId);
                return null;
            } else {
                DataNode dn = nn.getData();
                if( dn == null ) {
                    log.warn("no data node");
                    return null;
                } else {
                    if( dn instanceof User ) {
                        return (User) dn;
                    } else {
                        log.warn("not a user. is a: " + dn.getClass().getCanonicalName());
                        return null;
                    }
                }
            }
        }
    }

    public void setCreator( User user, BaseResource res ) {
        res.setCreatorNameNodeId( res.getNameNodeId());
        res.save();
    }

}
