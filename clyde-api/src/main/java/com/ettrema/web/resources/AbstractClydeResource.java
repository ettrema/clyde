package com.ettrema.web.resources;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.ettrema.vfs.DataNode;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.Date;
import java.util.UUID;
import com.ettrema.vfs.NameNode;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.ettrema.utils.RedirectService;
import com.ettrema.web.CachePolicyService;
import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public abstract class AbstractClydeResource implements CopyableResource, DeletableResource, GetableResource, MoveableResource, DataNode, PropFindableResource, Serializable {

    private static final Logger log = LoggerFactory.getLogger( AbstractClydeResource.class );
    /**
     * set by the framework in setId
     */
    private UUID dataId;
    /**
     * Hold a transient instance to the nameNode which owns this datanode. This
     * is set by the framework in init
     */
    private transient NameNode nameNode;

    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        log.info("delete: " + getName());
        nameNode.delete();
    }

    public String getUniqueId() {
        if( dataId == null ) {
            log.warn( "null dataId!! " + this.getName() );
            return null;
        } else {
            return dataId.toString();
        }
    }

    public String getName() {
        return nameNode.getName();
    }

    public Date getCreateDate() {
        return nameNode.getCreatedDate();
    }

    public void copyTo( CollectionResource toCollection, String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void moveTo( CollectionResource rDest, String name ) throws ConflictException {
        throw new UnsupportedOperationException( "Not supported yet." );
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

    public String checkRedirect( Request request ) {
        return _( RedirectService.class ).checkRedirect( this, request );
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return _( CachePolicyService.class ).getMaxAgeSeconds( this, auth );
    }

    public void setId( UUID id ) {
        log.debug( "setId: " + id);
        this.dataId = id;
    }

    public UUID getId() {
        return dataId;
    }

    public void init( NameNode nameNode ) {
        this.nameNode = nameNode;
    }

    public void onDeleted( NameNode nameNode ) {
    }

    public NameNode getNameNode() {
        return nameNode;
    }
}
