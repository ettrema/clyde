package com.ettrema.web.resources;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class FolderResource extends AbstractClydeResource implements CollectionResource, MakeCollectionableResource, PutableResource, Serializable {

    private static final Logger log = LoggerFactory.getLogger( FolderResource.class );

    private static final long serialVersionUID = 1L;


    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        // folder has no content
    }

    public String getContentType( String accepts ) {
        return null; // folder has no content type
    }

    public Long getContentLength() {
        // A folder has no content
        return null;
    }

    public Resource child( String childName ) {
        NameNode nChild = getNameNode().child( childName );
        if( nChild == null ) {
            return null;
        } else {
            DataNode dn = nChild.getData();
            if( dn == null ) {
                return null;
            } else {
                if( dn instanceof Resource ) {
                    return (Resource) dn;
                } else {
                    return null;
                }
            }
        }
    }

    public List<? extends Resource> getChildren() {
        return new ArrayList( getChildMap().values() );
    }

    public Map<String, ? extends Resource> getChildMap() {
        Map<String, Resource> children = new HashMap<String, Resource>();
        NameNode nThis = getNameNode();
        List<NameNode> list = nThis.children();
        if( list != null ) {
            for( NameNode n : nThis.children() ) {
                DataNode dn = n.getData();
                if( dn != null && dn instanceof Resource ) {
                    Resource res = (Resource) dn;
                    children.put( res.getName(), res );
                }
            }
        }

        return children;
    }

    public CollectionResource createCollection( String newName ) throws NotAuthorizedException, ConflictException, BadRequestException {
        FolderResource res = new FolderResource();
        NameNode newResourceNode = getNameNode().add( newName, res );
        newResourceNode.save();
        return res;
    }

    public Resource createNew( String newName, InputStream inputStream, Long length, String contentType ) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        log.info("createNew: " + newName);
        BinaryResource res = new BinaryResource();
        NameNode newResourceNode = getNameNode().add( newName, res );
        log.debug("created id: " + res.getId());
        newResourceNode.save();
        res.setContent( inputStream );
        newResourceNode.save(); // save content length
        return res;

    }
}
