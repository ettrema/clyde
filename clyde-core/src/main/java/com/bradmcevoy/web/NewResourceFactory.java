package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.utils.LogUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

public class NewResourceFactory extends CommonResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( NewResourceFactory.class );
    private ResourceFactory next;
    public static final String REDIRECT_TO_NEW_PATH = "_submitNew";

    public NewResourceFactory( ResourceFactory next, HostFinder hostFinder ) {
        super( hostFinder );
        this.next = next;
    }

    @Override
    public Resource getResource( String host, String url ) {
        LogUtils.trace(log, "getResource: host=", host, " url=", url);
        Path path = Path.path( url );
        if( REDIRECT_TO_NEW_PATH.equals( path.getName() ) ) {
            Path pRes = NewPage.getPagePath( path );
            Path folder = pRes.getParent();
            Resource r = next.getResource( host, folder.toString() );
            if( r == null ) {
                log.trace( "folder not found, not returning New page" );
                return null;
            } else {
                Folder f = (Folder) r;
                try {
                    return new RedirectToNewPage( path.getParent(), f.getRealm() );
                } catch( IllegalArgumentException e ) {
                    log.warn( "illegal argument", e );
                    return new NewResourceErrorPage( url, pRes.getName(), f, e.getMessage() );
                }
            }
        } else if( NewPage.isNewPath( path ) ) {
            Path pRes = NewPage.getPagePath( path );
            Path folder = pRes.getParent();
            Resource r = next.getResource( host, folder.toString() );
            if( r == null ) {
                log.debug( "folder not found, not returning New page" );
                return null;
            } else {
                if( r instanceof Folder ) {
                    Folder f = (Folder) r;
                    try {
                        return new NewPage( f, pRes.getName() );
                    } catch( IllegalArgumentException e ) {
                        log.warn( "illegal argument", e );
                        return new NewResourceErrorPage( url, pRes.getName(), f, e.getMessage() );
                    }
                } else {
                    return new NewResourceErrorPage( url, pRes.getName(), r, "Parent resource is not a folder" );
                }
            }
        } else {
            Resource r = next.getResource( host, url );
			LogUtils.trace(log, "got result:", r);
			return r;
        }
    }

    public class NewResourceErrorPage extends ErrorPage {

        public NewResourceErrorPage( String href, String name, Resource f, String msg ) {
            super( href, name, f, msg );
        }

        @Override
        protected void doBody( OutputStream out ) throws IOException {
            PrintWriter w = new PrintWriter( out, true );
            w.print( this.errorMessage );
            w.print( ": <b>" );
            w.print( name );
            w.print( "</b><br/>" );
            w.print( "<h3>Contents of this folder:</h3> " );
            w.print( "<ul>" );
            if( parent instanceof Folder ) {
                Folder folder = (Folder) this.parent;
                for( Resource res : folder.getChildren() ) {
                    w.print( "<li>" );
                    if( res instanceof CommonTemplated ) {
                        w.print( link( (CommonTemplated) res ) );
                    } else {
                        w.print( res.getName() );
                    }
                    w.print( "</li>" );
                }
            }
            w.print( "</ul>" );
            w.flush();
            w.close();
        }
    }

    public static String link( CommonTemplated res ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "<a href='" ).append( res.getHref() ).append( "'>" ).append( res.getName() ).append( "</a>" );
        return sb.toString();
    }

    public class RedirectToNewPage implements PostableResource, DigestResource {

        private String realm;
        private Path folderPath;

        private RedirectToNewPage( Path parent, String realm ) {
            folderPath = parent;
            this.realm = realm;
        }

        @Override
        public void sendContent( OutputStream arg0, Range arg1, Map<String, String> arg2, String contentType ) throws IOException, NotAuthorizedException {
        }

        @Override
        public Long getMaxAgeSeconds( Auth auth ) {
            return null;
        }

        @Override
        public String getContentType( String arg0 ) {
            return null;
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getName() {
            return REDIRECT_TO_NEW_PATH;
        }

        @Override
        public Object authenticate( String arg0, String arg1 ) {
            return arg0;
        }

        @Override
        public boolean authorise( Request arg0, Method arg1, Auth arg2 ) {
            return true;
        }

        @Override
        public String getRealm() {
            return realm;
        }

        @Override
        public Date getModifiedDate() {
            return null;
        }

        @Override
        public String checkRedirect( Request request ) {
            return null;
        }

        @Override
        public String processForm( Map<String, String> params, Map<String, FileItem> arg1 ) {
            String templateSelect = params.get( "templateSelect" );
            String newName = params.get( "newName" );
            return folderPath + "/" + newName + ".new?templateSelect=" + templateSelect;
        }

        @Override
        public Object authenticate( DigestResponse digestRequest ) {
            return digestRequest.getUser();
        }

        public boolean isDigestAllowed() {
            return true;
        }
    }
}
