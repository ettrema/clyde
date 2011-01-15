package com.bradmcevoy.web.groups;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.CurrentRequestService;
import com.bradmcevoy.utils.RelationUtils;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Group;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.security.CustomUserGroup;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import com.bradmcevoy.web.security.Subject;
import com.bradmcevoy.web.security.UserGroup;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.vfs.Relationship;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

import static com.ettrema.context.RequestContext._;

/**
 * Implements persistence of group information for clyde resources
 *
 *
 * @author brad
 */
public class RelationalGroupHelper implements GroupService, ClydeGroupHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RelationalGroupHelper.class );
    public static final String REL_NAME = "memberOf";
    private final GroupService wrapped = new SystemGroupService();
    private final PermissionChecker permissionChecker;

    public RelationalGroupHelper( PermissionChecker permissionChecker ) {
        this.permissionChecker = permissionChecker;
    }

    public List<Subject> getMembers( UserGroup group ) {
        log.trace( "getMembers" );
        NameNode nFrom;
        if( group instanceof Group ) {
            Group clydeGroup = (Group) group;
            List<Subject> list = new ArrayList<Subject>();
            List<Relationship> rels = clydeGroup.getNameNode().findToRelations( REL_NAME );
            if( rels != null && rels.size() > 0 ) {
                for( Relationship r : rels ) {
                    nFrom = r.from();
                    if( nFrom != null && nFrom.getData() instanceof Subject ) {
                        Subject subject = (Subject) nFrom.getData();
                        list.add( subject );
                    }
                }
            }
            return list;
        } else {
            return null;
        }
    }

    public void addToGroup( IUser user, CustomUserGroup group ) {
        log.trace( "addToGroup" );
        if( group == null ) {
            throw new NullPointerException( "group is null" );
        }
        if( group instanceof Group ) {
            Group clydeGroup = (Group) group;
            if( clydeGroup.isSecure() ) {
                Auth auth = _( CurrentRequestService.class ).request().getAuthorization();

                if( !permissionChecker.hasRole( Role.AUTHOR, clydeGroup, auth ) ) {
                    throw new RuntimeException( "The current user does not have permission to add users to this group. You must be an Author of the group" );
                }
            }
            Relationship r = user.getNameNode().makeRelation( clydeGroup.getNameNode(), REL_NAME );
            if( log.isTraceEnabled() ) {
                log.trace( "created relation from: " + r.from().getId() + " -> " + r.to().getId() );
            }
            clydeGroup.getNameNode().onNewRelationship( r );
        } else {
            throw new RuntimeException( "Cant add to: " + group.getClass() );
        }
    }

    public UserGroup getGroup( Resource relativeTo, String name ) {
        log.trace( "getGroup" );
        if( relativeTo instanceof Templatable ) {
            Templatable t = (Templatable) relativeTo;
            Group g = t.getHost().findGroup( name );
            if( g != null ) {
                return g;
            }
        }
        log.trace( "look for system group" );
        return wrapped.getGroup( relativeTo, name );
    }

    public boolean isInGroup( IUser user, Group group ) {
        log.trace( "isInGroup" );
        List<Relationship> rels = user.getNameNode().findFromRelations( REL_NAME );
        if( CollectionUtils.isEmpty( rels ) ) {
            return false;
        } else {
            for( Relationship r : rels ) {
                BaseResource res = RelationUtils.to( r );
                if( res instanceof Group ) {
                    Group g = (Group) res;
                    if( g.getNameNodeId().equals( group.getNameNodeId() ) ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void removeFromGroup( IUser user, Group group ) {
        log.trace( "removeFromGroup" );
        List<Relationship> rels = user.getNameNode().findFromRelations( REL_NAME );
        if( CollectionUtils.isEmpty( rels ) ) {
            return;
        } else {
            for( Relationship r : rels ) {
                BaseResource res = RelationUtils.to( r );
                if( res instanceof Group ) {
                    Group g = (Group) res;
                    if( g.getNameNodeId().equals( group.getNameNodeId() ) ) {
                        user.getNameNode().onDeletedFromRelationship( r );
                        RelationalNameNode groupNode = (RelationalNameNode) r.to();
                        groupNode.onDeletedToRelationship( r );
                        r.delete();
                    }
                }
            }
        }
    }
}
