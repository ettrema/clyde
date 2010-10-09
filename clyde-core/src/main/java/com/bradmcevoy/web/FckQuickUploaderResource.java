package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.common.UnrecoverableException;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.utils.FileUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 * @author Alienware1
 */
public class FckQuickUploaderResource extends FckCommon {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FckQuickUploaderResource.class );
    public final static String UPLOAD_RESPONSE_TEMPLATE_NORMAL = ""
        + "<script type=\"text/javascript\">\n"
        + "window.parent.frames['frmUpload'].OnUploadCompleted([code],'[name]') ;\n"
        + "</script>\n";
    public final static Path URL = Path.path( "/fck_upload" );
    //public final static Path URL = Path.path("/editor/filemanager/upload/ettrema/upload.ettrema");
    private int code;
    private String filename;

    public FckQuickUploaderResource( Host host ) {
        super( host, URL );
    }

    @Override
    public String getUniqueId() {
        return "fckquickuploader";
    }

    @Override
    public String processForm( Map<String, String> params, Map<String, FileItem> files ) {
        if( files == null || files.size() == 0 ) {
            log.warn( "no files to upload" );
            return null;
        }
        for( FileItem f : files.values() ) {
            processFileUpload( f, params );
        }
        return null;
    }

    private void processFileUpload( FileItem f, Map<String, String> params ) {
        Folder target = null;
        if( host == null ) {
            throw new UnrecoverableException( "host not found" );
        }
        target = (Folder) host.child( "uploads" );
        if( target == null ) {
            try {
                target = (Folder) host.createCollection( "uploads" );
            } catch( ConflictException ex ) {
                throw new RuntimeException( ex );
            } catch( NotAuthorizedException ex ) {
                throw new RuntimeException( ex );
            } catch( BadRequestException ex ) {
                throw new RuntimeException( ex );
            }
            target.save();
        }

        String name = FileUtils.sanitiseName( f.getName() );
        log.debug( "processFileUpload: " + name );
        boolean isFirst = true;
        String newName = null;
        while( target.hasChild( name ) ) {
            name = FileUtils.incrementFileName( name, isFirst );
            newName = name;
            isFirst = false;
        }

        long size = f.getSize();
        try {
            Resource newRes = target.createNew( name, f.getInputStream(), size, null );
        } catch( ConflictException ex ) {
            throw new RuntimeException( ex );
        } catch( NotAuthorizedException ex ) {
            throw new RuntimeException( ex );
        } catch( BadRequestException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }

        try {
            if( newName != null ) { // we renamed the file
                uploadResponseOk( name );
            } else {
                uploadResponseOk();
            }
            commit();
        } catch( Throwable ex ) {
            log.error( "Exception saving new file", ex );
            uploadResponseFailed( ex.getMessage() );
            rollback();
        }
    }

    private void uploadResponseOk() {
        uploadResponse( 0, null );

    }

    private void uploadResponseOk( String newName ) {
        uploadResponse( 201, newName );
    }

    private void uploadResponseFailed( String reason ) {
        uploadResponse( 1, reason );
    }

    private void uploadResponse( int code, String filename ) {
        this.code = code;
        this.filename = filename;
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        String s = UPLOAD_RESPONSE_TEMPLATE_NORMAL;
        s = s.replace( "[code]", code + "" );
        String f = filename == null ? "" : filename;
        s = s.replace( "[name]", f );
        out.write( s.getBytes() );
    }

    @Override
    public String getContentType( String accepts ) {
        return "text/html";
    }
}
