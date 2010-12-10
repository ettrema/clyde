package com.bradmcevoy.web.code;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.code.content.CodeUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class CodeFolder extends AbstractCodeResource<CollectionResource> implements CollectionResource, DeletableResource, PutableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CodeFolder.class );

    public CodeFolder( CodeResourceFactory rf, String name, CollectionResource wrapped ) {
        super( rf, name, wrapped );
    }

    public Resource child( String childName ) {
        log.trace( "child: " + childName );
        if( rf.isMeta( childName ) ) {
            String realName = rf.getPageName( childName );
            Resource realChild = wrapped.child( realName );
            log.trace("..meta");
            return rf.wrapMeta( realChild );
        } else {
            Resource child = wrapped.child( childName );
            if( child == null ) {
                log.trace("..not found");
                return null;
            } else if( child instanceof CollectionResource ) {
                log.trace("..col");
                return rf.wrapCollection( (CollectionResource) child );
            } else {
                log.trace("..content");
                return rf.wrapContent( (GetableResource) child );
            }
        }
    }

    public List<? extends Resource> getChildren() {
        List<Resource> list = new ArrayList<Resource>();
        for( Resource child : wrapped.getChildren() ) {
            Resource meta = rf.wrapMeta( child );
            if( meta != null ) {
                list.add(meta);
            }
            Resource wrappedChild;
            if( child instanceof CollectionResource ) {
                wrappedChild = rf.wrapCollection( (CollectionResource) child );
            } else {
                wrappedChild = rf.wrapContent( (GetableResource) child );
            }
            if( wrappedChild != null ) {
                list.add(wrappedChild);
            }
        }
        return list;
    }

    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        if( wrapped instanceof DeletableResource ) {
            ( (DeletableResource) wrapped ).delete();
            CodeUtils.commit();
        } else {
            throw new BadRequestException( this, "The resource is not deletable" );
        }
    }

    public Resource createNew( String newName, InputStream inputStream, Long length, String contentType ) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        log.trace( "createNew" );
        if( rf.isMeta( newName ) ) {
            newName = rf.getPageName( newName );
            Resource r = rf.getMetaParser().createNew( wrapped, newName, inputStream );
            CodeUtils.commit();
            return r;
        } else {
            throw new RuntimeException();
        }
    }
}
