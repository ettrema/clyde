package com.bradmcevoy.web.security;

import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.RelationalNameNode;
import com.bradmcevoy.vfs.Relationship;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

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

    public void grant( String roleName, PermissionRecipient user ) {
        grant(Role.valueOf( roleName ), user);
    }

    public void grant( Role role, PermissionRecipient user ) {
//        Permission p = new Permission(role, user.getNameNodeId());
//        add( p );
//        return p;
        // create a relationship to the user
        if( this.nameNode == null) throw new IllegalStateException( "name node is not set");
        if( user == null ) throw new IllegalArgumentException( "user is null");
        if( role == null ) throw new IllegalArgumentException( "role is null");
        this.nameNode.makeRelation( user.getNameNode(), role.toString() );
        if( role.equals( Role.ADMINISTRATOR) ) {
            grant(Role.AUTHOR, user);
        }
        if( role.equals( Role.AUTHOR)) {
            grant(Role.VIEWER, user);
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
        BaseResource granted = (BaseResource) this.nameNode.getParent().getData();
        List<Relationship> rels = this.nameNode.findFromRelations( null );
        if( rels == null || rels.size() == 0 ) return null;
        List<Permission> list = new ArrayList<Permission>();
        for( Relationship r : rels ) {
            Role role = Role.valueOf( r.relationship() );
            PermissionRecipient grantee = (PermissionRecipient) r.to().getData();
            if( grantee != null ) {
                Permission p = new Permission( role, grantee, granted );
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
//        log.debug( "allows: " + requestedRole );
        List<Relationship> rels = this.nameNode.findFromRelations( requestedRole.toString() );
        if( rels == null || rels.size() == 0 ) {
            log.debug( "no relations found: from: " + this.nameNode.getId() );
            return false;
        }

        for( Relationship r : rels ) {
            PermissionRecipient grantee = (PermissionRecipient) r.to().getData();
            if( grantee != null ) {
                User grantedUser = grantee.getUser();
                if( grantedUser != null && grantedUser.getNameNodeId().equals( user.getNameNodeId() ) ) {
                    return true;
                }
            }
        }
        return false;
    }
}
