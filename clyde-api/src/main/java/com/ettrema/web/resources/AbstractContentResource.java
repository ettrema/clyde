package com.ettrema.web.resources;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.utils.RedirectService;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.creation.CreatorService;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;
import java.util.Date;
import java.util.UUID;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public abstract class AbstractContentResource implements Resource, DataNode, CopyableResource, DeletableResource, GetableResource, MoveableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractContentResource.class);

    private static final long serialVersionUID = 1L;

    private transient RelationalNameNode nameNode;
    private transient IUser creator;
    
    private UUID id;
    private String contentType;

    /**
     * Instantiate and save, a copy of the current resource in the new folder
     * @param newParent
     * @return
     */
    protected abstract AbstractContentResource copyInstance(FolderResource newParent);

    public AbstractContentResource( String contentType, FolderResource parentFolder, String newName ) {
        if( newName.contains( "/" ) ) {
            throw new IllegalArgumentException( "Names cannot contain forward slashes" );
        }
        this.nameNode = parentFolder.onChildCreated( newName, this );
        setContentType( contentType );
    }

    @Override
    public String checkRedirect( Request request ) {
        return _(RedirectService.class).checkRedirect( this, request );
    }

    public String getName() {
        return nameNode.getName();
    }

    public RelationalNameNode getNameNode() {
        return nameNode;
    }

    public void setContentType( String contentType ) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentType( String accepts ) {
        return getContentType();
    }

    public String getUniqueId() {
        return id.toString();
    }

    public Date getModifiedDate() {
        return nameNode.getModifiedDate();
    }


    public Object authenticate( String user, String password ) {
        return _( com.bradmcevoy.http.SecurityManager.class ).authenticate( user, password );
    }

    public boolean authorise( Request request, Method method, Auth auth ) {
        return _( com.bradmcevoy.http.SecurityManager.class ).authorise( request, method, auth, this );
    }

    public String getRealm() {
        return _( com.bradmcevoy.http.SecurityManager.class ).getRealm( null );
    }

    public void setId( UUID id ) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void init( NameNode nameNode ) {
        this.nameNode = (RelationalNameNode) nameNode;
    }

    public void onDeleted( NameNode nameNode ) {
        
    }

    public void copyTo( CollectionResource toCollection, String name ) {
        if( toCollection instanceof FolderResource) {
            FolderResource fr = (FolderResource) toCollection;
            AbstractContentResource newRes = this.copyInstance( fr );
            log.info( "created new resource: " + newRes.getName());
        } else {
            throw new RuntimeException( "Unsupported destination: " + toCollection.getClass().getCanonicalName());
        }
    }

    public void moveTo( CollectionResource toCollection, String name ) throws ConflictException {
        if( toCollection instanceof FolderResource) {
            FolderResource fr = (FolderResource) toCollection;
            this.nameNode.move( fr.getNameNode(), name );
            this.save();
        } else {
            throw new RuntimeException( "Unsupported destination: " + toCollection.getClass().getCanonicalName());
        }
    }


    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        this.nameNode.delete();
    }

    public void save() {
        nameNode.save();
    }


    /**
     * Creates a relation from this to the user with name "creator"
     *
     * @param user
     */
    public void setCreator( IUser user ) {
        this.creator = user;
        _( CreatorService.class ).setCreator( user, this );
    }

    /**
     * looks for the relation created by setCreator and returns the associated
     * resource, which must be a User
     *
     * @return
     */
    public IUser getCreator() {
        // Something dodgy going on here. Seem to get different results wihthout
        // the transient variable
        if( this.creator == null ) {
            this.creator = _( CreatorService.class ).getCreator( this );
        }
        return creator;
    }

    public String getCreatorName() {
        IUser u = getCreator();
        if( u == null ) return null;
        return u.getName();
    }

    public String getCreatorExternalEmail() {
        IUser u = getCreator();
        if( u == null ) return null;
        return u.getExternalEmailText();
    }
}
