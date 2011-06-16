package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.utils.ReflectionUtils;
import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.security.ClydeAuthoriser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class SourceResourceFactory extends CommonResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SourceResourceFactory.class );
    private final ResourceFactory next;

    public SourceResourceFactory( ResourceFactory next, HostFinder hostFinder ) {
        super( hostFinder );
        this.next = next;
    }

    @Override
    public Resource getResource( String host, String url ) {
        log.debug( "getResource: " + url );
        Path path = Path.path( url );
        if( SourcePage.isSourcePath( path ) ) {
            Path pagePath = SourcePage.getPagePath( path );
            Resource res = next.getResource( host, pagePath.toString() );
            if( res == null ) {
                log.debug( "resource not found" );
                Resource rParent = next.getResource( host, pagePath.getParent().toString() );
                if( rParent == null ) {
                    return null;
                } else {
                    if( rParent instanceof Folder ) {
                        log.debug( "found a parent, and it is a folder" );
                        Folder fParent = (Folder) rParent;
                        return new NewResourceSourcePage( pagePath.getName(), fParent );
                    } else {
                        log.debug( "found parent, but its not a folder: " + rParent.getClass().getCanonicalName() );
                        return null;
                    }
                }
            } else if( res instanceof XmlPersistableResource ) {
                log.debug( "found a xmlpersistable resource: " );
                return new SourcePage( (XmlPersistableResource) res );
            } else {
                log.debug( "found a resource, but is not source: " + res.getClass() );
                return null;
            }
//        } else if( CodeBehindPage.isCodeBehind( path ) ) {
//            log.debug( "is code behind" );
//            Path pagePath = CodeBehindPage.getPagePath( path );
//            Resource res = next.getResource( host, pagePath.toString() );
//            if( res == null ) {
//                return null;
//            } else {
//                if( res instanceof BaseResource ) {
//                    return new CodeBehindPage( (BaseResource) res );
//                } else {
//                    log.debug( "not compat" );
//                    return null;
//                }
//            }

        } else {
            Resource res = next.getResource( host, url );
            return res;
        }
    }

    /**
     * Implements GetableResource so HEAD requests don't fail
     */
    public static class NewResourceSourcePage extends VfsCommon implements Replaceable, GetableResource, DigestResource {

        private final String name;
        private final Folder parent;

        public NewResourceSourcePage( String name, Folder parent ) {
            this.name = name;
            this.parent = parent;
        }

        @Override
        public void replaceContent( InputStream in, Long contentLength ) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                StreamUtils.readTo( in, out );
                XmlUtils2 x = new XmlUtils2();
                String s = out.toString();
                Document doc = x.getJDomDocument( s );
                Element el = doc.getRootElement();
                el = (Element) el.getChildren().get( 0 );

                String className = el.getAttributeValue( "class" );
                Resource r = (Resource) ReflectionUtils.create( className, parent, name );
                if( r instanceof XmlPersistableResource ) {
                    XmlPersistableResource xmlpr = (XmlPersistableResource) r;
                    xmlpr.loadFromXml( el, null );
                    xmlpr.save();
                    commit();
                } else {
                    throw new RuntimeException( "the requested class does not implement: " + XmlPersistableResource.class );
                }
            } catch( JDOMException ex ) {
                throw new RuntimeException( ex );
            } catch( ReadingException ex ) {
                throw new RuntimeException( ex );
            } catch( WritingException ex ) {
                throw new RuntimeException( ex );
            }
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object authenticate( String user, String password ) {
            return parent.authenticate( user, password );
        }

        public Object authenticate( DigestResponse digestRequest ) {
            return parent.authenticate( digestRequest );
        }

        public boolean isDigestAllowed() {
            return true;
        }

        @Override
        public boolean authorise( Request request, Request.Method method, Auth auth ) {
            ClydeAuthoriser authoriser = requestContext().get( ClydeAuthoriser.class );
            return authoriser.authorise( this, request, method, auth );
        }

        @Override
        public String getRealm() {
            return parent.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            return null;
        }

        @Override
        public String checkRedirect( Request request ) {
            return null;
        }

        public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        }

        public Long getMaxAgeSeconds( Auth auth ) {
            return null;
        }

        public String getContentType( String accepts ) {
            return null;
        }

        public Long getContentLength() {
            return null;
        }
    }
}
