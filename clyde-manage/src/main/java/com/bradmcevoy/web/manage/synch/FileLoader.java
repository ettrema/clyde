package com.bradmcevoy.web.manage.synch;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.Replaceable;
import com.bradmcevoy.web.code.CodeResourceFactory;
import com.ettrema.vfs.VfsSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class FileLoader {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FileLoader.class );
    private String hostName = "test.ppod.com";  // todo: fix config to use localhost
    //private String hostName = "127.0.0.1";
    private final File root;
    private final CodeResourceFactory resourceFactory;
    private final ErrorReporter errorReporter;

    public FileLoader( File root, CodeResourceFactory resourceFactory, ErrorReporter errorReporter ) {
        this.root = root;
        this.resourceFactory = resourceFactory;
        this.errorReporter = errorReporter;
    }

    public void onNewFile( File f ) {
        try {
            upload( f );
            _( VfsSession.class ).commit();
        } catch( Exception ex ) {
            _( VfsSession.class ).rollback();
            errorReporter.onError( f, ex );
        }
    }

    public void onDeleted( File f ) {
        try {
            delete( f );
            _( VfsSession.class ).commit();
        } catch( Exception ex ) {
            _( VfsSession.class ).rollback();
            errorReporter.onError( f, ex );
        }
    }

    public void onModified( File f ) {
        try {
            upload( f );
            _( VfsSession.class ).commit();
        } catch( Exception ex ) {
            _( VfsSession.class ).rollback();
            errorReporter.onError( f, ex );
        }

    }

    public void onRenamed( File f ) {
    }

    public boolean exists( File f ) {
        f = toMetaFile( f );
        Resource r = resourceFactory.getResource( hostName, toUrl( f ) );
        return r != null;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName( String hostName ) {
        this.hostName = hostName;
    }

    public CodeResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    private File toContentFile( File f ) {
        if( f.getName().endsWith( ".meta.xml" ) ) {
            String contentName = f.getName().replace( ".meta.xml", "" );
            return new File( f.getParentFile(), contentName );
        } else {
            return f;
        }
    }

    private File toMetaFile( File f ) {
        if( f.getName().endsWith( ".meta.xml" ) ) {
            return f;
        } else {
            String metaName = f.getName() + ".meta.xml";
            return new File( f.getParentFile(), metaName );

        }
    }

    private String toUrl( File f ) {
        String s = f.getAbsolutePath();
        if( s.startsWith( root.getAbsolutePath() ) ) {
            s = s.replace( root.getAbsolutePath(), "" );
            s = s.replace( "\\", "/" );
            s = "/_code" + s;
            return s;
        } else {
            return null;
        }
    }

    private void upload( File f ) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        log.trace( "upload: " + f.getAbsolutePath() );
        File fMeta = toMetaFile( f );
        if( fMeta.exists() ) {
            put( fMeta );
        }
        File fContent = toContentFile( f );
        if( fContent.exists() ) {
            if( fContent.isFile() ) {
                put( fContent );
            }
        }

    }

    /**
     * Just upload the given file to its parent directory.
     * No name transformations
     * @param f
     */
    private void put( File f ) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        CollectionResource colParent = findCollection( f.getParentFile() );
        Resource rExisting = colParent.child( f.getName() );
        FileInputStream fin = null;
        if( rExisting instanceof Replaceable ) {
            Replaceable replaceable = (Replaceable) rExisting;
            try {
                fin = new FileInputStream( f );
                replaceable.replaceContent( fin, f.length() );
            } catch( FileNotFoundException ex ) {
                throw new RuntimeException( ex );
            } finally {
                IOUtils.closeQuietly( fin );
            }
        } else {
            if( colParent instanceof PutableResource ) {
                PutableResource putable = (PutableResource) colParent;
                try {
                    fin = new FileInputStream( f );
                    putable.createNew( f.getName(), fin, f.length(), null );
                } catch( FileNotFoundException ex ) {
                    throw new RuntimeException( ex );
                } finally {
                    IOUtils.closeQuietly( fin );
                }

            }
        }
    }

    private CollectionResource findCollection( File f ) throws NotAuthorizedException, ConflictException, BadRequestException {
        log.trace( "findCollection: " + f.getAbsolutePath() );
        String url = toUrl( f );
        if( url == null ) {
            return null;
        }
        Resource r = resourceFactory.getResource( hostName, url );
        CollectionResource col;
        if( r == null ) {
            log.trace( "not found: " + url );
            Resource rParent = findCollection( f.getParentFile() );
            if( rParent == null ) {
                throw new RuntimeException( "Couldnt get parent: " + f.getAbsolutePath() );
            } else if( rParent instanceof MakeCollectionableResource ) {
                MakeCollectionableResource mkcol = (MakeCollectionableResource) rParent;
                col = mkcol.createCollection( f.getName() );
                return col;
            } else {
                throw new RuntimeException( "Cant create " + f.getAbsolutePath() + " parent doesnt support MKCOL" );
            }
        } else {
            if( r instanceof CollectionResource ) {
                return (CollectionResource) r;
            } else {
                throw new RuntimeException( "Found resource but its not a collection: " + f.getAbsolutePath() );
            }
        }


    }

    private void delete( File f ) throws NotAuthorizedException, ConflictException, BadRequestException {
        log.trace( "delete: " + f.getAbsolutePath() );
        File fMeta = toMetaFile( f );
        Resource r = resourceFactory.getResource( hostName, toUrl( fMeta ) );
        if( r == null ) {
            log.trace( "not found to delete" );
        } else if( r instanceof DeletableResource ) {
            DeletableResource dr = (DeletableResource) r;
            dr.delete();
        } else {
            throw new RuntimeException( "Cannot delete " + f.getAbsolutePath() + " is a : " + r.getClass() );
        }

    }
}
