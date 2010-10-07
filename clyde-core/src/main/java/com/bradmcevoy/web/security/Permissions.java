package com.bradmcevoy.web.security;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BaseResource.RoleAndGroup;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.groups.GroupService;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.vfs.Relationship;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import org.apache.commons.collections.CollectionUtils;

import static com.ettrema.context.RequestContext._;

/**
 * This class is used as an instance hanging off a BaseResource. ie It is associated
 * with a name node who's parent will be a BaseResource
 *
 * Permissions are represented as relationships from this to a PermissionRecipient
 * ie User or Invitation
 *
 * A Permission is a triplet, combining:
 *  - a resource to have a permission granted on,
 *  - a user to have the permission granted to
 *  - a role which the user is granted
 *
 * Roles are well known and hard coded. What they mean for any particular component
 * is determined by that component. For example, an editing component may require
 * the AUTHOR role to allow a user to write, but a comments component might allow
 * writes by users with only VIEWER permission.
 *
 *
 * @author brad
 */
public class Permissions implements List<Permission>, DataNode, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Permissions.class );
    private static final long serialVersionUID = 1L;
    public static final String NAME_NODE_KEY = "_sys_permissions";
    private UUID dataNodeId;
    private transient RelationalNameNode nameNode;
    private transient BaseResource _granted;

    public void grant( String roleName, Subject subject ) {
        grant( Role.valueOf( roleName ), subject );
    }

    public void grant( Role role, Subject subject ) {
        if( this.nameNode == null ) {
            throw new IllegalStateException( "name node is not set" );
        }
        if( subject == null ) {
            throw new IllegalArgumentException( "user is null" );
        }
        if( role == null ) {
            throw new IllegalArgumentException( "role is null" );
        }
        if( subject instanceof PermissionRecipient ) {
            PermissionRecipient res = (PermissionRecipient) subject;
            this.nameNode.makeRelation( res.getNameNode(), role.toString() );
        } else if( subject instanceof SystemUserGroup ) {
            RoleAndGroup rag = new RoleAndGroup( role, subject.getSubjectName() );
            addGroup( rag );
        } else {
            throw new RuntimeException( "Cant grant to subject of type: " + subject.getClass() );
        }
        if( role.equals( Role.ADMINISTRATOR ) ) {
            grant( Role.AUTHOR, subject );
        }
        if( role.equals( Role.AUTHOR ) ) {
            grant( Role.VIEWER, subject );
        }
    }

    /**
     * Revoke all roles for the given user on this resource
     *
     * @param user
     */
    public void revokeAll( User user ) {
        for( Role r : Role.values() ) {
            revoke( r, user );
        }
    }

    /**
     * Remove the given role for the given user on this resource, if any exist
     *
     * @param role
     * @param user
     */
    public void revoke( Role role, User user ) {
        List<Relationship> rels = this.nameNode.findFromRelations( role.toString() );
        for( Relationship r : rels ) {
            if( r.to().getId().equals( user.getNameNodeId() ) ) {
                r.delete();
            }
        }
    }

    private List<Permission> list() {
        List<Permission> list = new ArrayList<Permission>();
        List<Relationship> rels = this.nameNode.findFromRelations( null );
        if( !CollectionUtils.isEmpty( rels ) ) {
            for( Relationship r : rels ) {
                String roleName = r.relationship();
                try {
                    Role role = Role.valueOf( roleName );
                    PermissionRecipient grantee = (PermissionRecipient) r.to().getData();
                    if( grantee != null ) {
                        Permission p = new Permission( role, grantee, granted() );
                        list.add( p );
                    }
                } catch( IllegalArgumentException e ) {
                    log.warn( "Invalid role: " + roleName );
                }
            }
        }

        for( RoleAndGroup rag : granted().getGroupPermissions() ) {
            UserGroup group = _( GroupService.class ).getGroup( granted(), rag.getGroupName() );
            if( group != null ) {
                Permission p = new Permission( rag.getRole(), group, granted() );
                list.add( p );
            }
        }
        return list;
    }

    @Override
    public int size() {
        return list().size();
    }

    @Override
    public boolean isEmpty() {
        return list().isEmpty();
    }

    @Override
    @SuppressWarnings( "element-type-mismatch" )
    public boolean contains( Object o ) {
        return list().contains( o );
    }

    @Override
    public Iterator<Permission> iterator() {
        return list().iterator();
    }

    @Override
    public Object[] toArray() {
        return list().toArray();
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        return list().toArray( a );
    }

    @Override
    public boolean add( Permission e ) {
        return list().add( e );
    }

    @Override
    @SuppressWarnings( "element-type-mismatch" )
    public boolean remove( Object o ) {
        return list().remove( o );
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        return list().containsAll( c );
    }

    @Override
    public boolean addAll( Collection<? extends Permission> c ) {
        return list().addAll( c );
    }

    @Override
    public boolean addAll( int index, Collection<? extends Permission> c ) {
        return list().addAll( index, c );
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        return list().removeAll( c );
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        return list().retainAll( c );
    }

    @Override
    public void clear() {
        list().clear();
    }

    @Override
    public Permission get( int index ) {
        return list().get( index );
    }

    @Override
    public Permission set( int index, Permission element ) {
        return list().set( index, element );
    }

    @Override
    public void add( int index, Permission element ) {
        list().add( index, element );
    }

    @Override
    public Permission remove( int index ) {
        return list().remove( index );
    }

    @Override
    public int indexOf( Object o ) {
        return list().indexOf( o );
    }

    @Override
    public int lastIndexOf( Object o ) {
        return list().lastIndexOf( o );
    }

    @Override
    public ListIterator<Permission> listIterator() {
        return list().listIterator();
    }

    @Override
    public ListIterator<Permission> listIterator( int index ) {
        return list().listIterator( index );
    }

    @Override
    public List<Permission> subList( int fromIndex, int toIndex ) {
        return list().subList( fromIndex, toIndex );
    }

    @Override
    public void setId( UUID id ) {
        this.dataNodeId = id;
    }

    @Override
    public UUID getId() {
        return this.dataNodeId;
    }

    @Override
    public void init( NameNode nameNode ) {
        this.nameNode = (RelationalNameNode) nameNode;
    }

    @Override
    public void onDeleted( NameNode nameNode ) {
    }

    /**
     * Return true if this permissions object has a Permission which
     * allows the given role for the given user.
     *
     * Non-recursive, only queries this object's relationships.
     *
     * @param user - the user requesting access
     * @param role - the access being requested
     * @return - true if the user should be permitted the requested access
     */
    public boolean allows( User user, Role requestedRole ) {
        if( log.isTraceEnabled() ) {
            log.trace( "allows: " + requestedRole );
        }
        List<Relationship> rels = this.nameNode.findFromRelations( requestedRole.toString() );
        if( !CollectionUtils.isEmpty( rels ) ) {
            for( Relationship r : rels ) {
                NameNode to = r.to();
                if( to != null ) {
                    PermissionRecipient grantee = (PermissionRecipient) to.getData();
                    if( grantee != null ) {
                        IUser grantedUser = grantee.getUser();
                        if( grantedUser != null && grantedUser.getNameNodeId().equals( user.getNameNodeId() ) ) {
                            log.trace( "found granted user" );
                            return true;
                        }
                    }
                }
            }
        }
        for( RoleAndGroup rag : granted().getGroupPermissions() ) {
            if( rag.getRole() == requestedRole ) {
                UserGroup group = _( GroupService.class ).getGroup( granted(), rag.getGroupName() );
                if( group != null ) {
                    log.trace( "found group with role" );
                    if( group.isInGroup( user ) ) {
                        log.trace( "user is in group" );
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void addGroup( RoleAndGroup rag ) {
        log.trace( "addGroup: " + rag.getGroupName() );
        granted().getGroupPermissions().add( rag );
        granted().save();
    }

    private BaseResource granted() {
        if( _granted == null ) {
            _granted = (BaseResource) nameNode.getParent().getData();
        }
        return _granted;
    }
}
