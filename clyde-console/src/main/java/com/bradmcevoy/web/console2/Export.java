package com.bradmcevoy.web.console2;

import com.bradmcevoy.common.FrameworkBase;
import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.DateUtils.DateParseException;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.TextFile;
import com.bradmcevoy.web.XmlPersistableResource;
import com.ettrema.console.Result;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class Export extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Export.class );

    public Export( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        Arguments arguments;
        try {
            arguments = new Arguments( args );
        } catch( Exception ex ) {
            log.error( "parse", ex );
            return result( "Couldnt parse request arguments: " + ex.getMessage() );
        }
        try {
            return doImport( arguments );
        } catch( Exception e ) {
            log.error( "exception in export", e );
            return result( e.getMessage() + arguments.getReport() );
        }

    }

    private Result doImport( Arguments arguments ) throws Exception {
        log.debug( "doImport" );
        Folder folder = this.currentResource();
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive( true );
        Credentials defaultcreds = new UsernamePasswordCredentials( arguments.destUser, arguments.destPassword );
        client.getState().setCredentials( new AuthScope( null, -1, null ), defaultcreds );

        importFolder( folder, client, arguments, arguments.destPath );

        return result( "ok: " + arguments.getReport() );
    }

    private void importFolder( Folder folder, HttpClient client, Arguments arguments, String destPath ) throws Exception {
        log.debug( "importFolder: " + folder.getHref() );
        for( Resource r : folder.getChildren() ) {
            if( r instanceof Templatable ){
                Templatable ct = (Templatable) r;
                if( isImportable( ct, arguments ) ) {
                    doImport( (XmlPersistableResource) ct, client, destPath, arguments );
                    if( arguments.recursive ) {
                        if( ct instanceof Folder && !( ct instanceof Host ) ) {
                            importFolder( (Folder) ct, client, arguments, destPath + "/" + ct.getName() );
                        }
                    }
                } else {
                    log.debug( "not processing: " + ct.getHref() );
                }
            }
        }
    }

    private void doImport( XmlPersistableResource res, HttpClient client, String path, Arguments arguments ) throws Exception {
        log.debug( "doImport: " + res.getHref() + " - path:" + path );
        Date localDate = res.getModifiedDate();
        RemoteResource remote = new RemoteResource( arguments.destHost, path, res, client );
        Date destDate = remote.doHead();

        if( destDate == null || localDate.after( destDate ) ) {
            arguments.uploaded( res, destDate );
            if( arguments.dryRun ) return;

            if( remote.doPut() ) return;
        } else {
            arguments.skipped( res, destDate );
            log.debug( "not uploading: " + res.getHref() );
        }

    }

    private boolean isImportable( Templatable ct, Arguments arguments ) {
        if( ct instanceof XmlPersistableResource ) {
            if( ct instanceof BaseResource) {
                BaseResource bres = (BaseResource) ct;
                if( bres.isTrash()) return false;
            }
            if( ct instanceof Host ) {
                return !arguments.stopAtHosts;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    class ContentSender extends FrameworkBase {

        void send( OutputStream out, XmlPersistableResource res ) {
            Document doc = new Document( new Element( "res" ) );
            res.toXml( doc.getRootElement(), null );
            utilXml().saveXMLDocument( out, doc );
        }
    }

    static class Arguments {

        final String destHost;
        final String destUser;
        final String destPassword;
        final String destPath;
        boolean dryRun;
        boolean recursive;
        boolean stopAtHosts;
        final List<FileExportStatus> statuses = new ArrayList<FileExportStatus>();

        public Arguments( List<String> args ) throws Exception {
            List<String> list = new ArrayList<String>( args );
            Iterator<String> it = list.iterator();
            while( it.hasNext() ) {
                String s = it.next();
                if( s.startsWith( "-" ) ) {
                    processOption( s );
                    it.remove();
                }
            }
            if( list.size() < 3 ) {
                throw new Exception( "not enough arguments" );
            }
            String dest = list.get( 0 );
            URI uriDest = new URI( dest );
            destHost = uriDest.getHost();
            destPath = uriDest.getPath();
            destUser = list.get( 1 );
            destPassword = list.get( 2 );
        }

        private void processOption( String s ) {
            if( s.equals( "-dry" ) ) {
                dryRun = true;
            } else if( s.equals( "-r" ) ) {
                recursive = true;
            } else if( s.equals( "-nohost" ) ) {
                stopAtHosts = true;
            }
        }

        String getReport() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XmlWriter w = new XmlWriter( out );
            w.begin( "p" ).writeText( "remote host: " + destHost ).close();
            w.begin( "p" ).writeText( "remote user: " + destUser ).close();
            w.begin( "p" ).writeText( "  dryRun: " + dryRun ).close();
            w.begin( "p" ).writeText( "  recursive: " + recursive ).close();
            w.begin( "p" ).writeText( "  stop at hosts: " + stopAtHosts ).close();

            w.begin( "h3" ).writeText( "Uploaded" ).close();
            XmlWriter.Element elTable = w.begin( "table" );
            elTable.writeAtt( "width", "100%" );
            XmlWriter.Element elHeadRow = elTable.begin( "tr" );
            elHeadRow.begin( "th" ).writeText( "local href" ).close();
            elHeadRow.begin( "th" ).writeText( "local mod date" ).close();
            elHeadRow.begin( "th" ).writeText( "remote mod date" ).close();
            elHeadRow.close();
            for( FileExportStatus fileStat : statuses ) {
                if( fileStat.uploaded ) {
                    XmlWriter.Element elRow = elTable.begin( "tr" );
                    elRow.begin( "td" ).writeText( fileStat.localRes.getHref() ).close();
                    elRow.begin( "td" ).writeText( formatDate( fileStat.localRes.getModifiedDate() ) ).close();
                    if( fileStat.remoteMod != null ) {
                        elRow.begin( "td" ).writeText( formatDate( fileStat.remoteMod ) ).close();
                    } else {
                        elRow.begin( "td" ).writeText( "na" ).close();
                    }
                    elRow.close();
                }
            }
            elTable.close();

            w.begin( "h3" ).writeText( "Skipped" ).close();
            elTable = w.begin( "table" );
            elTable.writeAtt( "width", "100%" );
            elHeadRow = elTable.begin( "tr" );
            elHeadRow.begin( "th" ).writeText( "local href" ).close();
            elHeadRow.begin( "th" ).writeText( "local mod date" ).close();
            elHeadRow.begin( "th" ).writeText( "remote mod date" ).close();
            elHeadRow.close();
            for( FileExportStatus s : statuses ) {
                if( !s.uploaded ) {
                    XmlWriter.Element elRow = elTable.begin( "tr" );
                    elRow.begin( "td" ).writeText( s.localRes.getHref() ).close();
                    elRow.begin( "td" ).writeText( formatDate( s.localRes.getModifiedDate() ) ).close();
                    elRow.begin( "td" ).writeText( formatDate( s.remoteMod ) ).close();
                    elRow.close();
                }
            }
            elTable.close();
            w.flush();

            String s = out.toString();
            return s;
        }

        private String formatDate( Date dt ) {
            return DateUtils.formatDate( dt );
        }

        private void uploaded( XmlPersistableResource r, Date remoteMod ) {
            FileExportStatus s = new FileExportStatus( r, remoteMod, true );
            statuses.add( s );
        }

        private void skipped( XmlPersistableResource r, Date remoteMod ) {
            FileExportStatus s = new FileExportStatus( r, remoteMod, false );
            statuses.add( s );
        }
    }

    static class FileExportStatus {

        final XmlPersistableResource localRes;
        final Date remoteMod;
        final boolean uploaded;

        public FileExportStatus( XmlPersistableResource r, Date remoteMod, boolean uploaded ) {
            this.localRes = r;
            this.remoteMod = remoteMod;
            this.uploaded = uploaded;
        }
    }

    public class RemoteResource {

        String destHost;
        String destFolder;
        XmlPersistableResource res;
        HttpClient client;
        String uri;
        String sourceUri;

        public RemoteResource( String destHost, String destFolder, XmlPersistableResource res, HttpClient client ) {
            this.destHost = destHost;
            this.destFolder = destFolder;
            this.res = res;
            this.client = client;
            uri = "http://" + destHost;
            uri = uri + destFolder;
            log.debug( "uri2: " + uri );
            if( !uri.endsWith( "/") ) uri += "/";
            uri = uri + res.getName();
            sourceUri = uri + ".source";

        }

        /**
         *
         * @return - modified date
         */
        Date doHead() throws Exception {
            log.debug( "doHead: " + uri );
            String sModDate = "";
            try {
                HeadMethod headMethod = new HeadMethod( uri );
                headMethod.setFollowRedirects( false );
                int result = client.executeMethod( headMethod );
                if( result == 404 ) return null;

                if( result == 302 ) { // if redirect, then the folder exists, so get its source
                    headMethod = new HeadMethod( sourceUri );
                    result = client.executeMethod( headMethod );
                }

                checkError( result );
                Header headerModDate = headMethod.getResponseHeader( "Last-Modified" );
                if( headerModDate == null ) {
                    log.debug( "no last-mod header" );
                    return null;
                }
                sModDate = headerModDate.getValue();
                log.debug( "mod date: " + sModDate );
                log.debug( "result: " + result );
                return DateUtils.parseDate( sModDate );
            } catch( DateParseException ex ) {
                throw new RuntimeException( "bad date: " + sModDate );
            } catch( HttpException ex ) {
                throw new RuntimeException( ex );
            } catch( IOException ex ) {
                throw new RuntimeException( ex );
            }
        }

        boolean doPut() throws RuntimeException, Exception {
            try {
                PutMethod putMethod;
                RequestEntity entity;
                int result;

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ContentSender cs = new ContentSender();
                cs.send( out, res );

                putMethod = new PutMethod( sourceUri );
                entity = new ByteArrayRequestEntity( out.toByteArray() );
                putMethod.setRequestEntity( entity );
                result = client.executeMethod( putMethod );
                checkError( result );
                if( res instanceof BinaryFile ) {
                    BinaryFile bf = (BinaryFile) res;
                    putMethod = new PutMethod( uri );
                    entity = new InputStreamRequestEntity( bf.getInputStream() );
                    putMethod.setRequestEntity( entity );
                    result = client.executeMethod( putMethod );
                    checkError( result );
                } else if( res instanceof TextFile ) {
                    TextFile tf = (TextFile) res;
                    putMethod = new PutMethod( uri );
                    entity = new StringRequestEntity( tf.getContent(), tf.getContentType( null ), "UTF-8" );
                    putMethod.setRequestEntity( entity );
                    result = client.executeMethod( putMethod );
                    checkError( result );
                }
                log.debug( "done put: " + this.uri);
            } catch( HttpException ex ) {
                throw new RuntimeException( ex );
            } catch( IOException ex ) {
                throw new RuntimeException( ex );
            }
            return false;
        }

        private void checkError( int result ) throws Exception {
            if( result < 200 || result > 299 ) {
                throw new Exception( "Remote error: " + result );
            }
        }
    }
}
