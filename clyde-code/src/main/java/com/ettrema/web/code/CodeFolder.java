package com.ettrema.web.code;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.code.content.CodeUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class CodeFolder extends AbstractCodeResource<CollectionResource> implements CollectionResource, DeletableResource, PutableResource, MakeCollectionableResource, MoveableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CodeFolder.class );

    public CodeFolder( CodeResourceFactory rf, String name, CollectionResource wrapped ) {
        super( rf, name, wrapped );
    }

	@Override
    public Resource child( String childName ) {
        log.trace( "child: " + childName );
        if( rf.isMeta( childName ) ) {
            String realName = rf.getPageName( childName );
            Resource realChild = wrapped.child( realName );
            log.trace( "..meta" );
            return rf.wrapMeta( realChild, this.wrapped );
        } else {
            Resource child = wrapped.child( childName );
            if( child == null ) {
                log.trace( "..not found" );
                return null;
            } else if( child instanceof CollectionResource ) {
                log.trace( "..col" );
                return rf.wrapCollection( (CollectionResource) child );
            } else {
                log.trace( "..content" );
                return rf.wrapContent( (GetableResource) child );
            }
        }
    }

	@Override
    public List<? extends Resource> getChildren() {
        List<Resource> list = new ArrayList<Resource>();
        for( Resource child : wrapped.getChildren() ) {
            Resource meta = rf.wrapMeta( child, this.wrapped );
            if( meta != null ) {
                list.add( meta );
            }
            Resource wrappedChild;
            if( child instanceof CollectionResource ) {
                wrappedChild = rf.wrapCollection( (CollectionResource) child );
            } else {
                wrappedChild = rf.wrapContent( (GetableResource) child );
            }
            if( wrappedChild != null ) {
                list.add( wrappedChild );
            }
        }
        return list;
    }

	@Override
    public Resource createNew( String newName, InputStream inputStream, Long length, String contentType ) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        log.trace( "createNew" );
        if( rf.isMeta( newName ) ) {
            newName = rf.getPageName( newName );
            Resource r = rf.getMetaParser().createNew( wrapped, newName, inputStream );
            CodeUtils.commit();
            return r;
        } else {
            if( wrapped instanceof PutableResource ) {
                PutableResource putableResource = (PutableResource) wrapped;
                Resource r = putableResource.createNew( newName, inputStream, length, contentType );
                return rf.wrapContent( (GetableResource) r);
            } else {
                log.error( "Can't create new resource, wrapped collection does not allow PUT" );
                throw new ConflictException( wrapped );
            }
        }
    }

	@Override
    public CollectionResource createCollection( String newName ) throws NotAuthorizedException, ConflictException, BadRequestException {
        if( wrapped instanceof MakeCollectionableResource) {
            MakeCollectionableResource mk = (MakeCollectionableResource) wrapped;
            return this.rf.wrapCollection(mk.createCollection( newName ));
        } else {
            throw new ConflictException( wrapped);
        }
    }

	@Override
    public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
        if( rDest instanceof CodeFolder ) {
            if( this.wrapped instanceof MoveableResource ) {
                MoveableResource source = (MoveableResource) this.wrapped;
                CodeFolder cfDest = (CodeFolder) rDest;
                source.moveTo( cfDest.wrapped, name );
            } else {
                throw new ConflictException( rDest );
            }
        } else {
            throw new ConflictException( rDest );
        }
    }


}
