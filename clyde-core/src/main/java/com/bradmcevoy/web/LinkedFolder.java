package com.bradmcevoy.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.Relationship;
import java.util.Collections;
import java.util.List;

/**
 * Implements a collectionresource whose children is that of another linked folder
 *
 * The linked folder is determined by the linkedto property
 *
 * @author brad
 */
public class LinkedFolder extends BaseResource implements CollectionResource, GetableResource, PropFindableResource, XmlPersistableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( LinkedFolder.class );
    private static final long serialVersionUID = 1L;
    public static String REL_LINKED_TO = "_sys_linked_to";

    public LinkedFolder( Folder parentFolder, String newName ) {
        super( null, parentFolder, newName );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean isIndexable() {
        return false;
    }

    @Override
    public void onDeleted( NameNode nameNode ) {
    }

    @Override
    public Resource child( String childName ) {
        for( Resource r : getChildren() ) {
            if( r.getName().equals( childName ) ) {
                return r;
            }
        }
        return null;
    }

    public void setLinkedTo( Folder cr ) {
        List<Relationship> rels = this.getNameNode().findToRelations( REL_LINKED_TO );

        // delete previous relations
        if( rels != null && rels.size() > 0 ) {
            for( Relationship rel : rels ) {
                rel.delete();
            }
        }
        // create new relation
        Relationship newRel = this.getNameNode().makeRelation( cr.getNameNode(), REL_LINKED_TO );
        cr.getNameNode().onNewRelationship( newRel );
    }

    public Folder getLinkedTo() {
        List<Relationship> rels = this.getNameNode().findToRelations( REL_LINKED_TO );
        if( rels == null || rels.size() == 0 ) {
            return null;
        } else {
            if( rels.size() > 1 ) {
                log.warn( "multiple relations found, using first only" );
            }
            Relationship rel = rels.get( 0 );
            NameNode nTo = rel.to();
            if( nTo == null ) {
                log.warn( "to node does not exist" );
                return null;
            } else {
                DataNode dnTo = nTo.getData();
                if( dnTo == null ) {
                    log.warn( "to node has no data" );
                    return null;
                } else {
                    if( dnTo instanceof Folder ) {
                        Folder cr = (Folder) dnTo;
                        return cr;
                    } else {
                        log.warn( "to node is not a: " + Folder.class + " is a: " + dnTo.getClass() );
                        return null;
                    }
                }
            }
        }
    }

    @Override
    public List<? extends Resource> getChildren() {
        CollectionResource cr = getLinkedTo();
        if( cr == null ) {
            return Collections.EMPTY_LIST;
        } else {
            return cr.getChildren();
        }
    }

    @Override
    public boolean authorise( Request request, Method method, Auth auth ) {
        log.trace( "authorise: " + auth );
        boolean result;
        // Certain methods delegate to the wrapped collection, eg PROPFIND. While others apply to this link eg DELETE
        if( method.equals( Method.DELETE ) ) {
            log.trace( "is a delete, so authorise against this resource");
            result = super.authorise( request, method, auth );
        } else {
            Folder linkedTo = getLinkedTo();
            if( linkedTo == null ) {
                log.trace( "couldnt find linkedTo, so authorise against this link");
                result = super.authorise( request, method, auth );
            } else {
                log.trace( "forward auth request to target");
                result = linkedTo.authorise( request, method, auth );
            }
        }
        log.trace( "authorise: " + auth + " -> " + result );
        return result;
    }
}
