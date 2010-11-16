package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.common.UnrecoverableException;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.Utils;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.utils.FileUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public class FckFileManagerResource extends FckCommon implements GetableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FckFileManagerResource.class );
    public final static Path URL = Path.path( "/fck_connector.html" );
    //public final static Path URL = Path.path("/editor/filemanager/browser/default/connectors/ettrema/connector.html");
    public final static String UPLOAD_RESPONSE_TEMPLATE_NORMAL = ""
        + "<script type=\"text/javascript\">\n"
        + "window.parent.frames['frmUpload'].OnUploadCompleted([code],'[name]') ;\n"
        + "</script>\n";
    public final static String UPLOAD_RESPONSE_TEMPLATE = ""
        + "<script type='text/javascript'>\n"
        + "    window.parent.OnUploadCompleted( '[code]', '[msg]' ) ;\n"
        + "</script>\n";
    private FckPostParams uploadParams;

    public FckFileManagerResource( CollectionResource folder ) {
        super( folder, URL );
    }

    @Override
    public String getUniqueId() {
        return "fckeditor";
    }

    @Override
    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) {
        uploadParams = new FckPostParams( parameters );
        uploadParams.processFileUploadCommand( files );
        return null;
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        log.debug( "sendContent" );
        if( uploadParams != null ) {
            String s;
            if( uploadParams.code == 0 ) {
                s = UPLOAD_RESPONSE_TEMPLATE_NORMAL;
            } else {
                s = UPLOAD_RESPONSE_TEMPLATE;
            }
            s = s.replace( "[code]", uploadParams.code + "" );
            if( uploadParams.message == null ) {
                uploadParams.message = "";
            }
            s = s.replace( "[msg]", uploadParams.message );
            if( uploadParams.newName == null ) {
                uploadParams.newName = "";
            }
            s = s.replace( "[name]", uploadParams.newName );
            out.write( s.getBytes() );
        } else {
            FckGetParams getParams = new FckGetParams( out, params );
            getParams.process();
        }
    }

    /** See FCK <a href="http://wiki.fckeditor.net/Developer's_Guide/Participating/Server_Side_Integration">Integration Guide</a>
     *
     * fckconfig.js -> FCKConfig.LinkUploadURL
     */
    abstract class FckParams {

        PutableResource target;   // from params
        final Map<String, String> params;

        FckParams( Map<String, String> params ) {
            this.params = params;
        }
    }

    class FckGetParams extends FckParams {

        String command;
        String resourceType;
        String sFolder;
        String serverPath;
        String newFolderName;
        private XmlWriter writer;
        private OutputStream out;

        FckGetParams( OutputStream out, Map<String, String> params ) {
            super( params );
            this.out = out;
            writer = new XmlWriter( out );
            command = params.get( "Command" );
            resourceType = params.get( "Type" );
            sFolder = params.get( "CurrentFolder" );
            newFolderName = params.get( "NewFolderName" );
            if( sFolder != null ) {
                sFolder = sFolder.trim();
                if( sFolder.length() == 0 ) {
                    sFolder = null;
                }
            }
            serverPath = params.get( "ServerPath" );
        }

        void process() {
            String relFolder = sFolder.substring( 1 );
            Path p = Path.path( relFolder );
            Resource r = find(wrappedResource, p );
            if( r instanceof Folder ) {
                target = (Folder)r;
            }
            if( target == null ) {
                throw new UnrecoverableException( "No folder with that path: " + sFolder );
            }
            try {
                if( command.equals( "GetFolders" ) ) {
                    processGetFoldersCommand( false );
                } else if( command.equals( "GetFoldersAndFiles" ) ) {
                    processGetFoldersCommand( true );
                } else if( command.equals( "CreateFolder" ) ) {
                    processCreateFolderCommand();
                } else if( command.equals( "FileUpload" ) ) {
                    processUploadFolderCommand();
                } else {
                    throw new UnrecoverableException( "Unknown command: " + command );
                }
            } catch( UnrecoverableException ex ) {
                log.error( "Exception processing file uploader command:", ex );
            } finally {
                writer.flush();
            }
        }

        void initXml() {
            writer().writeXMLHeader();
        }

        void processGetFoldersCommand( boolean includeFiles ) {
            initXml();
            XmlWriter.Element el = writer().begin( "Connector" );
            el.writeAtt( "command", command );
            el.writeAtt( "resourceType", resourceType );
            el.open();
            el = writer().begin( "CurrentFolder" );
            el.writeAtt( "path", sFolder );
            el.writeAtt( "url", sFolder );
            el.noContent();
            writer().open( "Folders" );
            writer().writeText( "\n" );
            for( Resource r : target.getChildren() ) {
                if( r instanceof Folder ) {
                    el = writer().begin( "Folder" );
                    String nm = Utils.escapeXml( r.getName() );
                    el.writeAtt( "name", nm );
                    el.noContent();
                }
            }
            writer().close( "Folders" );

            if( includeFiles ) {
                writer().open( "Files" );
                writer().writeText( "\n" );
                for( Resource r : target.getChildren() ) {
                    if( !( r instanceof Folder ) ) {
                        el = writer().begin( "File" );
                        String nm = Utils.escapeXml( r.getName() );
                        el.writeAtt( "name", nm );
                        if( r instanceof GetableResource ) {
                            GetableResource gr = (GetableResource) r;
                            Long sz = gr.getContentLength();
                            String sSize = ( sz == null ? "" : sz.toString() );
                            el.writeAtt( "size", sSize );
                            el.noContent();
                        } else {
                            el.writeAtt( "size", "" );
                        }
                    }
                }
                writer().close( "Files" );
            }
            writer().close( "Connector" );
            writer().flush();
        }

        void processCreateFolderCommand() {
            log.debug( "processCreateFolderCommand: " + newFolderName );
            int errNumber;
            try {
                if( target.child( newFolderName ) != null ) {
                    log.debug( "has child" );
                    errNumber = 101;
                } else {
                    if( target instanceof MakeCollectionableResource) {
                        MakeCollectionableResource mk = (MakeCollectionableResource) target;
                        CollectionResource f = mk.createCollection( newFolderName );
                    } else {
                        throw new BadRequestException( target, "Folder does not allow creating subfolders");
                    }
                    log.debug( "add new child ok" );
                    errNumber = 0;
                }
            } catch( Throwable e ) {
                errNumber = 103;
                log.error( "Exception creating new folder: " + newFolderName + " in " + target.getName(), e );
                rollback();
            }
            initXml();
            XmlWriter.Element el = writer().begin( "Connector" );
            el.writeAtt( "command", command );
            el.writeAtt( "resourceType", resourceType );
            el.open();
            el = writer().begin( "CurrentFolder" );
            el.writeAtt( "path", sFolder );
            el.writeAtt( "url", sFolder );
            el.noContent();
            el = writer().begin( "Error" );
            el.writeAtt( "number", "" + errNumber );
            el.noContent();
            writer().close( "Connector" );
            writer().flush();
        }

        XmlWriter writer() {
            return writer;
        }

        private void processUploadFolderCommand() {
            if( uploadParams == null ) {
                throw new NullPointerException( "no post for upload command" );
            }
            StringBuilder sb = new StringBuilder();
            sb.append( "<script type='text/javascript'>\n" );
            sb.append( "window.parent.frames['frmUpload'].OnUploadCompleted(" + uploadParams.code );
            if( uploadParams.message != null ) {
                sb.append( ",'" + uploadParams.message + "'" );
            }
            sb.append( ");\n" );
            sb.append( "</script>\n" );
            String s = sb.toString();
            try {
                out.write( s.getBytes() );
                out.flush();
            } catch( IOException e ) {
                log.warn( "ioexception writing response to upload", e );
            }
        }
    }

    private Resource find( CollectionResource wrappedResource, Path p ) {
        Resource r = wrappedResource;
        for( String s : p.getParts() ) {
            if( r instanceof CollectionResource ) {
                CollectionResource col = (CollectionResource) r;
                r = col.child( s );
                if( r == null ) {
                    log.trace("not found: " + s + " in path: " + p);
                    return null;
                }
            } else {
                log.trace("not a collection: " + r.getName() + " in path: " + p);
                return null;
            }
        }
        return r;
    }

    class FckPostParams extends FckParams {

        int code;
        String message;
        String newName;

        FckPostParams( Map<String, String> params ) {
            super( params );
        }

        void processFileUploadCommand( Map<String, FileItem> files ) {
            Collection<FileItem> col = files.values();
            if( col == null || col.isEmpty() ) {
                log.debug( "no files uploaded" );
            } else {
                log.debug( "files: " + col.size() );
                for( FileItem f : col ) {
                    processFileUpload( f );
                    break;  // only file at a time

                }
            }
        }

        private void processFileUpload( FileItem f ) {
            String sFolder = params.get( "CurrentFolder" );
            log.debug( "sFolder: " + sFolder + " - " + sFolder.length() );
            String relFolder = sFolder.substring( 1 );
            Path p = Path.path( relFolder );
            Resource r = find( wrappedResource, p );
            if( r instanceof PutableResource ) {
                target = (PutableResource)r;
            }
            if( target == null ) {
                throw new UnrecoverableException( "No putable folder with that path: " + sFolder );
            }

            String name = f.getName(); //utilFile().sanitiseName(f.getName());
            log.debug( "processFileUpload: " + name );
            boolean isFirst = true;
            while( target.child( name ) != null ) {
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

        private void uploadResponse( int code, String message ) {
            this.code = code;
            this.message = message;
        }
    }

    @Override
    public String getContentType( String accepts ) {
        String s;
        if( uploadParams != null ) {
            s = Response.HTTP;
        } else {
            s = Response.XML;
        }
        return s;
    }
}
