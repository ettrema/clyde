package com.bradmcevoy.web;

import com.bradmcevoy.event.DeleteEvent;
import com.bradmcevoy.event.EventManager;
import com.bradmcevoy.event.PostSaveEvent;
import com.bradmcevoy.event.PreSaveEvent;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.utils.ReflectionUtils;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.DateDef;
import com.bradmcevoy.web.component.NameInput;
import com.bradmcevoy.web.component.TemplateSelect;
import com.bradmcevoy.web.component.Text;
import com.bradmcevoy.web.security.Permissions;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.EmptyDataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.vfs.Relationship;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdom.Element;

/**
 * Base class for all physical resources. Encapsulates a namenode and is a datanode
 * 
 * 
 * @author brad
 */
public abstract class BaseResource extends CommonTemplated implements DataNode, Addressable, XmlPersistableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( BaseResource.class );
    private static final long serialVersionUID = 1L;

    private UUID id;
    protected NameInput nameInput;

    private transient boolean nameInited;
    transient RelationalNameNode nameNode;
    private transient User creator;


    protected abstract BaseResource newInstance( Folder parent, String newName );



    /**
     * If this should be indexed for searching
     * 
     * @return
     */
    public abstract boolean isIndexable();



    public static BaseResource importResource( BaseResource parent, Element el, String filename ) {
        String className = el.getAttributeValue( "class" );
        if( className == null || className.length() == 0 )
            throw new IllegalArgumentException( "class is empty: " + filename );
        BaseResource res = (BaseResource) ReflectionUtils.create( className, parent, filename );
        res.loadFromXml( el );
        return res;
    }

    /** For root only
     */
    protected BaseResource() {
        super();
    }

    /** Usual constructor;
     */
    public BaseResource( String contentType, Folder parentFolder, String newName ) {        
        if( newName.contains( "/" ) )
            throw new IllegalArgumentException( "Names cannot contain forward slashes" );
        this.nameNode = (RelationalNameNode) parentFolder.onChildCreated( newName, this );
        setContentType( contentType );
        initName();

    }

    public boolean isNew() {
        return nameNode instanceof Folder.TransientNameNode;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    protected void initName() {
        ComponentMap map = this.getComponents();
        if( !map.containsKey( "name" ) ) {
            nameInited = true;
            nameInput = new NameInput( this );
            map.add( nameInput );    // everyone has a name component        
        }
    }

    public NameNode addChildNode( String name, DataNode dn ) {
        return this.nameNode.add( name, dn );
    }

    public void removeChildNode( String name ) {
        NameNode child = this.nameNode.child( name );
        if( child == null ) return;
        child.delete();
    }

    public RelationalNameNode getNameNode() {
        return nameNode;
    }

    /**
     * The ID of the name node which contains this resource
     *
     * @return
     */
    public UUID getNameNodeId() {
        if( nameNode == null ) return null;
        return nameNode.getId();
    }

    /**
     * For a physical resource, getParentFolder returns exactly the same result
     * as getParent
     *
     * @return
     */
    @Override
    public Folder getParentFolder() {
        return getParent();
    }

    /**
     * Move this resource to the given folder (aka collection) with the given name
     *
     * The name may be unchanged for a conventional move, or the collection might be
     * unchanged for a conventional rename operation, but both can be changed
     * simultaneously
     *
     * This method commits the transaction
     *
     * @param rDest
     * @param name
     */
    public void moveTo( CollectionResource rDest, String name ) {
        moveTo( rDest, name, true );
    }

    public void moveTo( CollectionResource rDest, String name, boolean commit ) {
        if( rDest instanceof Folder ) {
            Folder fDest = (Folder) rDest;
            _moveTo( fDest, name );
        } else {
            throw new RuntimeException( "destination collection is not a known type. Is a: " + rDest.getClass().getName() );
        }
        if( commit ) {
            commit();
        }
    }

    public void _moveTo( Folder fDest, String name ) {
        log.debug( "moveTo: name: " + name );
        this.nameNode.move( fDest.nameNode, name );
//        if( !fDest.getPath().equals(getParent().getPath()) ) {
//            log.debug("..moving folder to: " + fDest.getHref());
//            this.nameNode.move(fDest.nameNode, name);
//        }
        save();
    }

    /**
     * Copy to the given folder (ie collection) with the given name
     *
     * This method commits the transaction
     *
     * @param toCollection
     * @param name
     */
    public void copyTo( CollectionResource toCollection, String name ) {
        log.debug( "copyTo: from " + this.getName() + " to " + toCollection.getName() + ":" + name );
        if( toCollection instanceof Folder ) {
            Folder newParent = (Folder) toCollection;
            _copyTo( newParent, name );
            commit();
        } else {
            throw new IllegalArgumentException( "toCollection is not of type folder. Is: " + toCollection.getClass() );
        }
    }

    /**
     * Performs a copy to the given folder with the current name
     *
     * does not commit
     *
     * @param newParent
     */
    public void _copyTo( Folder newParent ) {
        _copyTo( newParent, this.getName() );
    }

    public void _copyTo( Folder newParent, String name ) {
        BaseResource newRes = copyInstance( newParent, name );
        newRes.templateSelect = (TemplateSelect) newRes.componentMap.get( "template" );
        newRes.save();
        log.debug( "created: " + newRes.getName() + " - " + newRes.getClass() );
    }

    protected BaseResource copyInstance( Folder parent, String newName ) {
        BaseResource newRes = newInstance( parent, newName );
        newRes.setContentType( this.getContentType( null) );
        newRes.valueMap.addAll( this.valueMap );
        newRes.componentMap.addAll( this.componentMap );
        String email = this.getExternalEmailTextV2( "default");
        newRes.setExternalEmailTextV2( "default", email);
        return newRes;
    }

    /**
     * Delete the resource and commit
     *
     */
    @Override
    public void delete() {
        deleteNoTx();
        commit();
    }

    /**
     * Delete without committing the transaction
     *
     */
    public void deleteNoTx() {
        log.debug( "delete: " + this.getName() );
        _delete();
        EventManager mgr = requestContext().get( EventManager.class );
        if( mgr != null ) {
            mgr.fireEvent( new DeleteEvent( this ) );
        }
    }

    /**
     * called from delete(). moves to trash, unless its already in trash or
     * is a host
     *
     * we don't put Host's in trash because they define their own Trash folder
     */
    public final void _delete() {
        if( this instanceof Host ) {
            log.debug( "physically deleting host: " + this.getName() );
            nameNode.delete();
        } else {
            if( this.getHost().isDisabled() ) {
                log.debug( "physically delete because host is disabled" );
                nameNode.delete();

            } else {
                Folder trashFolder = getTrashFolder();
                if( isTrash() ) {
                    log.debug( "physically delete trash item" );
                    deletePhysically();
                } else {
                    moveWithRename( this, trashFolder );
                }
            }
        }
    }

    /**
     * Physically delete by calling delete on the namenode
     *
     * Does not commit
     *
     */
    public void deletePhysically() {
        nameNode.delete();
    }

    /**
     * Will move the resource to the given folder, renaming if necessary
     *
     * @param res
     * @param folder
     */
    private void moveWithRename( BaseResource res, Folder target ) {
        log.debug( "moveWithRename: res: " + res.getName() + " target: " + target.getHref() );
        String name = res.getName();
        boolean isFirst = true;
        while( target.hasChild( name ) ) {
            name = FileUtils.incrementFileName( name, isFirst );
            isFirst = false;
        }
        log.debug( "  - renaming to: " + name );
        res.moveTo( target, name, false );

    }

    public final Element toXml( Element el ) {
        log.debug( "toXml" );
        Element e2 = new Element( "res" );
        el.addContent( e2 );
        populateXml( e2 );
        Element elHelp = new Element( "help" );
        el.addContent( elHelp );
        Element elDesc = new Element( "description" );
        elHelp.addContent( elDesc );
        elDesc.setText( getHelpDescription() );
        Element elAtts = new Element( "attributes" );
        elDesc.addContent( elAtts );
        Map<String, String> mapOfAttributes = new HashMap<String, String>();
        populateHelpAttributes( mapOfAttributes );
        for( Map.Entry<String, String> entry : mapOfAttributes.entrySet() ) {
            Element elAtt = new Element( "attribute" );
            elAtts.addContent( elAtt );
            elAtt.setAttribute( "name", entry.getKey() );
            elAtt.setText( entry.getValue() );
        }

        return e2;
    }

    @Override
    public void populateXml( Element e2 ) {
        e2.setAttribute( "name", getName() );
        e2.setAttribute( "id", getId().toString() );
        e2.setAttribute( "nameNodeId", getNameNodeId().toString() );        
        Element elRels = new Element( "relations" );
        e2.addContent( elRels );
        Element elFrom = new Element( "from" );
        elRels.addContent( elFrom );
        for( Relationship r : this.getNameNode().findFromRelations( null ) ) {
            Element elRel = new Element( "relationship" );
            elFrom.addContent( elRel );
            elRel.setAttribute( "relationship", r.relationship() );
            NameNode nTo = r.to();
            if( nTo != null ) {
                elRel.setAttribute( "id", nTo.getId().toString() );
                elRel.setAttribute( "name", nTo.getName() );
            }
        }

        Element elTo = new Element( "to" );
        elRels.addContent( elTo );
        for( Relationship r : this.getNameNode().findToRelations( null ) ) {
            Element elRel = new Element( "relationship" );
            elFrom.addContent( elRel );
            elRel.setAttribute( "relationship", r.relationship() );
            NameNode nFrom = r.from();
            if( nFrom != null ) {
                elRel.setAttribute( "id", nFrom.getId().toString() );
                elRel.setAttribute( "name", nFrom.getName() );
            }
        }
        super.populateXml( e2 );
    }

    @Override
    protected void populateHelpAttributes( Map<String, String> mapOfAttributes ) {
        super.populateHelpAttributes( mapOfAttributes );
        mapOfAttributes.put( "name", "the name of this persisted resource" );
        mapOfAttributes.put( "id", "the data node id" );
        mapOfAttributes.put( "nameNodeId", "the name node id" );

    }

    @Override
    public void save() {
        preSave();

        EventManager mgr = requestContext().get( EventManager.class );
        if( mgr != null ) {
            mgr.fireEvent( new PreSaveEvent( this ) );
        }

        nameNode.save();

        if( mgr != null ) {
            mgr.fireEvent( new PostSaveEvent( this ) );
        }

        afterSave();
    }

    /**
     * Does not commit or call save
     * 
     * @param newName
     */
    public void rename( String newName ) {
        log.debug( "rename: " + newName );
        nameNode.setName( newName );
    }

    public Text getNameInput() {
        Component c = this.getComponent( "name" );
        if( c == null ) {
            log.warn( "no name component: " + this.getPath() );
            return null;
        }
        if( c instanceof Text ) {
            return (Text) c;
        } else {
            log.warn( "name input exists, but is not a " + Text.class + ". Is a: " + c.getClass() );
            return null;
        }
    }

    @Override
    public void setId( UUID id ) {
        this.id = id;
    }

    @Override
    public void init( NameNode nameNode ) {
        this.nameNode = (RelationalNameNode) nameNode;
        getComponents().init( this );
        initName();
        getValues().init( this );
    }

    /** Need to ensure components know who their parents are. this information
     *  is not persisted, and is only held in transient variables which may
     *  be lost if passivated to a disk cache
     * 
     */
    protected void initComponents() {
        getComponents().init( this );
        getValues().init( this );
    }

    @Override
    public void loadFromXml( Element el, Map<String, String> params ) {
        loadFromXml( el );
    }

    @Override
    public Element toXml( Element el, Map<String, String> params ) {
        return toXml( el );
    }

    /**
     * The folder which contains this physical resource
     *
     * @return
     */
    @Override
    public Folder getParent() {
        if( nameNode == null ) return null;
        if( nameNode.getParent() == null ) return null;
        return (Folder) nameNode.getParent().getData();
    }

    @Override
    public String getName() {
        return nameNode.getName();
    }

    @Override
    public String getRealm() {
        return getHost().getName();
    }

    @Override
    public Date getModifiedDate() {
        Date thisModDate = nameNode.getModifiedDate();
        return thisModDate;
        // using template date messes up resource synchronisation

//        ITemplate t = this.getTemplate();
//        if( t != null ) {
//            Date templateModDate = t.getModifiedDate();
//            return Utils.mostRecent( templateModDate, thisModDate );
//        } else {
//            return thisModDate;
//        }
    }

    public String getModifiedDateFormatted() {
        return DateDef.sdf.format( getModifiedDate() );
    }

    public Long getModifiedDateAsLong() {
        Date dt = getModifiedDate();
        if( dt == null ) {
            return null;
        } else {
            return dt.getTime();
        }
    }

    @Override
    public Date getCreateDate() {
        return nameNode.getCreatedDate();
    }

    @Override
    public UUID getId() {
        return id;
    }

    public Web getParentWeb() {
        Folder f = getParent();
        if( f == null ) {
            log.debug( "no parent: " + getName() );
            return null;
        }
        return f.getWeb();
    }

    void setContent( InputStream in ) {
        if( in == null ) return;
        throw new UnsupportedOperationException( "Not implemented for base class" );
    }

    /**
     * Does nothing. Place holder. Called immediately before saving the node
     */
    protected void preSave() {
    }

    /**
     * Called immediatley after saving the node, if no exception
     */
    protected void afterSave() {
        boolean didChange = false;
        for( ComponentValue cv : this.getValues().values() ) {
            didChange = didChange || cv.afterSave();
        }
        if( didChange ) {
            this.save(); // careful, watch for recursion
        }
    }

    public Folder getTrashFolder() {
        return getParent().getTrashFolder();
    }

    private Relationship getRelationNode( String relationName ) {
        List<Relationship> list = nameNode.findFromRelations( relationName );
        if( list == null || list.size() == 0 ) return null;
        Relationship r = list.get( 0 );
        return r;
    }

    public BaseResource getRelation( String relationName ) {
        Relationship r = getRelationNode( relationName );
        if( r == null ) return null;
        NameNode toNode = r.to();
        BaseResource toRes = (BaseResource) toNode.getData();
        return toRes;
    }

    public BaseResourceList getRelations( String relationName ) {
        BaseResourceList resList = new BaseResourceList();
        List<Relationship> list = nameNode.findFromRelations( relationName );
        if( list == null || list.size() == 0 ) return resList;

        for( Relationship r : list ) {
            BaseResource res = (BaseResource) r.to().getData();
            resList.add( res );
        }
        return resList;
    }

    public BaseResourceList getToRelations( String relationName ) {
        BaseResourceList resList = new BaseResourceList();
        List<Relationship> list = nameNode.findToRelations( relationName );
        if( list == null || list.size() == 0 ) return resList;

        for( Relationship r : list ) {
            BaseResource res = (BaseResource) r.from().getData();
            resList.add( res );
        }
        return resList;
    }

    public void createRelationship( String relationName, BaseResource to ) {
        removeRelationship( relationName );
        this.nameNode.makeRelation( to.nameNode, relationName );
    }

    public void removeRelationship( String relationName ) {
        Relationship r = getRelationNode( relationName );
        if( r == null ) return;
        r.delete();
    }

    public BaseResource findByNameNodeId( UUID id ) {
        NameNode nn = this.vfs().get( id );
        return (BaseResource) nn.getData();
    }

//    @Override
//    public LockResult lock(LockTimeout timeout, LockInfo lockInfo) {
//        LockToken token = new LockToken();
//        token.tokenId = UUID.randomUUID().toString();
//        return LockResult.success(token);
//    }
//
//    /**
//     * Renew the lock and return new lock info
//     *
//     * @param token
//     * @return
//     */
//    @Override
//    public LockResult refreshLock(String token) {
//        LockToken t = new LockToken();
//        t.tokenId = UUID.randomUUID().toString();
//        return LockResult.success(t);
//
//    }
//
//    @Override
//    public void unlock(String tokenId) {
//
//    }
    public boolean isTrash() {
        for( Templatable t : this.getParents() ) {
            if( t.getName().equals( "Trash" ) ) return true;
        }
        return false;
    }

    public Permissions permissions() {
        return permissions( false );
    }

    public Permissions permissions( boolean create ) {
        NameNode nnPermissions = this.nameNode.child( Permissions.NAME_NODE_KEY );
        Permissions p;
        if( nnPermissions == null ) {
            if( create ) {
                p = new Permissions();
                nnPermissions = this.nameNode.add( Permissions.NAME_NODE_KEY, p );
                nnPermissions.save();
            } else {
                p = null;
            }
        } else {
            DataNode dn = nnPermissions.getData();
            if( dn != null ) {
                if( dn instanceof Permissions ) {
                    p = (Permissions) nnPermissions.getData();
                } else {
                    log.warn( "found: " + Permissions.NAME_NODE_KEY + " but is not a Permissions class. Is a: " + dn.getClass() + " resource nnid:" + this.getNameNodeId() );
                    p = null;
                }
            } else {
                log.warn( "found: " + Permissions.NAME_NODE_KEY + " but it has no data node associated. resource nnid:" + this.getNameNodeId() );
                p = null;
            }
        }
        return p;
    }

    /**
     * Creates a relation from this to the user with name "creator"
     *
     * @param user
     */
    public void setCreator( User user ) {
        this.creator = user;
        this.nameNode.makeRelation( user.getNameNode(), "creator" );
    }

    /**
     * looks for the relation created by setCreator and returns the associated
     * resource, which must be a User
     *
     * @return
     */
    public User getCreator() {
        // Something dodgy going on here. Seem to get different results wihthout
        // the transient variable
        if( this.creator == null ) {
            List<Relationship> rels = this.nameNode.findFromRelations( "creator" );
            if( rels == null || rels.size() == 0 ) return null;
            creator = (User) rels.get( 0 ).to().getData();
        }
        return creator;
    }

    public String getMagicNumber() {
        return this.getNameNodeId().hashCode() + "";
    }



    /**
     * Get the email address persisted in a child name node for the given category
     *
     * @param emailCategory - eg default, personal, business
     * @return
     */
    public String getExternalEmailTextV2(String emailCategory) {
//        log.debug( "getExternalEmailTextV2: " + emailCategory + " nnid: " + this.getNameNodeId());
        NameNode nEmailContainer = this.nameNode.child( "_email_" + emailCategory);
        if( nEmailContainer == null ) {
            log.warn( "no container");
            return null;
        }
        for( NameNode child : nEmailContainer.children() ) {
            DataNode childData = child.getData();
            if( childData instanceof EmailAddress ) {
                return child.getName();
            }
        }
        return null;
    }

    /**
     * Stores the email address in a child namenode for fast efficient lookups
     *
     * @param emailCategory - the category of this email address. Eg default, personal, business
     * @param email
     */
    public void setExternalEmailTextV2( String emailCategory, String email ) {
//        log.debug( "setExternalEmailTextV2: " + emailCategory + " email:" + email);

        NameNode nEmailContainer = this.nameNode.child( "_email_" + emailCategory);
        if( nEmailContainer == null ) {
            nEmailContainer = nameNode.add( "_email_" + emailCategory, new EmptyDataNode());
            nEmailContainer.save();
        }
        List<NameNode> children = new ArrayList<NameNode>(nEmailContainer.children());
        for( NameNode child : children ) {
            DataNode childData = child.getData();
            if( childData instanceof EmailAddress ) {
                child.delete();
            }
        }
        if( email != null && email.length() > 0) {
            NameNode nEmail = nEmailContainer.add( email, new EmailAddress() );
            nEmail.save();
        }
    }

}
