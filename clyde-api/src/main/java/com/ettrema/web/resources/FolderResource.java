package com.ettrema.web.resources;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.web.creation.ContentCreator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class FolderResource extends AbstractContentResource implements CollectionResource, PutableResource {

    public FolderResource( FolderResource parentFolder, String newName ) {
        super( null, parentFolder, newName );
    }

    public RelationalNameNode onChildCreated( String newName, AbstractContentResource baseResource ) {
        return (RelationalNameNode) getNameNode().add( newName, baseResource );
    }

    @Override
    protected AbstractContentResource copyInstance( FolderResource newParent ) {
        FolderResource newFolder = new FolderResource( newParent, this.getName() );
        newFolder.save();
        for( Resource r : this.getChildren() ) {
            if( r instanceof AbstractContentResource ) {
                AbstractContentResource child = (AbstractContentResource) r;
                AbstractContentResource newChild = child.copyInstance( newFolder );
            }
        }
        return newFolder;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        // no content for a folder
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public Long getContentLength() {
        return null;
    }

    public Resource child( String childName ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public List<? extends Resource> getChildren() {
        List<AbstractContentResource> children = new ArrayList<AbstractContentResource>();
        NameNode nThis = getNameNode();
        if( nThis != null ) {
            List<NameNode> list = nThis.children();
            if( list != null ) {
                for( NameNode n : nThis.children() ) {
                    DataNode dn = n.getData();
                    if( dn != null && dn instanceof AbstractContentResource ) {
                        AbstractContentResource res = (AbstractContentResource) dn;
                        children.add( res );
                    }
                }
            }
        }
        return children;
    }

    public Resource createNew( String newName, InputStream in, Long length, String contentType ) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        Resource rExisting = child( newName );
        if( rExisting != null ) {
            if( rExisting instanceof AbstractContentResource ) {
                ( (AbstractContentResource) rExisting ).delete();
            } else {
                throw new RuntimeException( "Cannot delete: " + rExisting.getClass().getName() );
            }
        }
        return doCreate( contentType, in, newName, length );
    }

    private Resource doCreate( String ct, InputStream in, String newName, Long length ) throws ReadingException, WritingException {
        return _( ContentCreator.class ).createResource( this, ct, in, newName );
    }
}
