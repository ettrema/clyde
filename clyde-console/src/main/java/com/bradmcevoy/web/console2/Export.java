package com.bradmcevoy.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.TextFile;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.XmlPersistableResource;
import com.bradmcevoy.web.recent.RecentResource;
import com.ettrema.console.Result;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class Export extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Export.class );
    private com.ettrema.httpclient.Host remoteHost;

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
            return result( e.getMessage() + arguments.getReport() + "<br/><p style='color: red'>ERRORS OCCURRED!!!</p>" );
        }

    }

    private Result doImport( Arguments arguments ) throws Exception {
        log.debug( "doImport" );
        Folder folder = this.currentResource();
        remoteHost = new com.ettrema.httpclient.Host( arguments.destHost, 80, arguments.destUser, arguments.destPassword, null );

        Path destPath = Path.path( arguments.destPath );
        importFolder( folder, arguments, destPath );

        return result( "ok: " + arguments.getReport() );
    }

    private void importFolder( Folder folder, Arguments arguments, Path destPath ) throws Exception {
        log.debug( "importFolder: " + folder.getHref() );
        for( Resource r : folder.getChildren() ) {
            if( r instanceof Templatable ) {
                Templatable ct = (Templatable) r;
                if( isImportable( ct, arguments ) ) {
                    doImport( (XmlPersistableResource) ct, destPath, arguments );
                    if( arguments.recursive ) {
                        if( ct instanceof Folder && !( ct instanceof Host ) ) {
                            importFolder( (Folder) ct, arguments, destPath.child( ct.getName() ) );
                        }
                    }
                } else {
                    log.debug( "not processing: " + ct.getHref() );
                }
            }
        }
    }

    private void doImport( XmlPersistableResource res, Path path, Arguments arguments ) throws Exception {
        log.debug( "doImport: " + res.getHref() + " - path:" + path );
        Date localDate = res.getModifiedDate();
        RemoteResource remote = new RemoteResource( path, res );
        Date destDate = remote.getModifiedDate();

        if( destDate == null || localDate.after( destDate ) ) {
            if( arguments.dryRun ) {
                arguments.uploaded( res, destDate );
                return;
            } else {
                try {
                    remote.doPut();
                    arguments.uploaded( res, destDate );
                } catch( Exception e ) {
                    log.warn( "Failed to put: " + remote.getRemotePath(), e );
                    arguments.skipped( res, destDate, "Upload failed - " + e.getMessage() );
                }
            }
        } else {
            arguments.skipped( res, destDate, "Remote file is newer" );
            log.debug( "not uploading: " + res.getHref() + " because of modified dates: local: " + localDate + " remote:" + destDate );
        }
    }

    private boolean isImportable( Templatable ct, Arguments arguments ) {
        if( ct instanceof XmlPersistableResource ) {
            if( ct instanceof RecentResource ) {
                return false;
            }
            if( ct instanceof BaseResource ) {
                BaseResource bres = (BaseResource) ct;
                if( bres.isTrash() ) return false;
            }
            if( ct instanceof User ) {
                if( arguments.noUser ) {
                    return false;
                }
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

    class ContentSender {

        void send( OutputStream out, XmlPersistableResource res ) {
            DocType docType = new DocType( "res",
                "-//W3C//ENTITIES Latin 1 for XHTML//EN",
                "http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent" );

            Document doc = new Document( new Element( "res" ), docType );
            try {
                res.toXml( doc.getRootElement(), null );
                XmlUtils2 utilXml = new XmlUtils2();
                utilXml.saveXMLDocument( out, doc );
            } catch( Throwable e ) {
                throw new RuntimeException( "Exception generating xml for resource: " + res.getHref(), e );
            }
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
        boolean noUser;
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
            } else if( s.equals( "-nouser" ) ) {
                noUser = true;
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
                    elRow.begin( "td" ).writeText( s.comment ).close();
                    elRow.close();
                }
            }
            elTable.close();
            w.flush();

            String s = out.toString();
            return s;
        }

        private String formatDate( Date dt ) {
            if( dt == null ) {
                return "";
            } else {
                return DateUtils.formatDate( dt );
            }
        }

        private void uploaded( XmlPersistableResource r, Date remoteMod ) {
            FileExportStatus s = new FileExportStatus( r, remoteMod, true, "" );
            statuses.add( s );
        }

        private void skipped( XmlPersistableResource r, Date remoteMod, String reason ) {
            FileExportStatus s = new FileExportStatus( r, remoteMod, false, reason );
            statuses.add( s );
        }
    }

    static class FileExportStatus {

        final XmlPersistableResource localRes;
        final Date remoteMod;
        final boolean uploaded;
        final String comment;

        public FileExportStatus( XmlPersistableResource r, Date remoteMod, boolean uploaded, String comment ) {
            this.localRes = r;
            this.remoteMod = remoteMod;
            this.uploaded = uploaded;
            this.comment = comment;
        }
    }

    public class RemoteResource {

        Path destFolder;
        XmlPersistableResource res;

        public RemoteResource( Path destFolder, XmlPersistableResource res ) {
            this.destFolder = destFolder;
            this.res = res;
        }

        /**
         *
         * @return - modified date
         */
        Date getModifiedDate() throws Exception {
            log.debug( "doHead: " + destFolder );
            com.ettrema.httpclient.Resource r = remoteHost.find( destFolder + "/" + res.getName() );
            if( r == null ) {
                log.trace( "not found: " + getRemotePath() );
                return null;
            }
            return r.getModifiedDate();
        }

        void doPut() throws Exception {
            log.warn( "put: " + this.getRemotePath() );
            com.ettrema.httpclient.Folder parent = remoteHost.getOrCreateFolder( destFolder, true );
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ContentSender cs = new ContentSender();
                cs.send( out, res );

                InputStream in = new ByteArrayInputStream( out.toByteArray() );
                parent.upload( res.getName() + ".source", in, (long) out.size() );

                if( res instanceof BinaryFile ) {
                    BinaryFile bf = (BinaryFile) res;
                    InputStream inContent = null;
                    try {
                        inContent = bf.getInputStream();
                        parent.upload( res.getName(), inContent, bf.getContentLength() );
                    } finally {
                        IOUtils.closeQuietly( in );
                    }
                } else if( res instanceof TextFile ) {
                    TextFile tf = (TextFile) res;
                    InputStream inContent = null;
                    try {
                        inContent = new ByteArrayInputStream( tf.getContent().getBytes( "UTF-8" ) );
                        parent.upload( res.getName(), inContent, tf.getContentLength() );
                    } finally {
                        IOUtils.closeQuietly( in );
                    }
                }
                log.debug( "done put: " + this.getRemotePath() );
            } catch( Exception ex ) {
                throw new Exception( "sourceUri:" + res.getHref(), ex );
            }
        }

        private Path getRemotePath() {
            return destFolder.child( res.getName() );
        }
    }
}
