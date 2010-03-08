package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.utils.ReflectionUtils;
import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.security.ClydeAuthoriser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class SourceResourceFactory extends CommonResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SourceResourceFactory.class );
    private final ResourceFactory next;

    public SourceResourceFactory( ResourceFactory next ) {
        this.next = next;
    }

    @Override
    public Resource getResource( String host, String url ) {
        Path path = Path.path( url );
        if( SourcePage.isSourcePath( path ) ) {
            Path pagePath = SourcePage.getPagePath( path );
            Resource res = next.getResource( host, pagePath.toString() );
            if( res == null ) {
                log.debug( "resource not found");
                return null;
            } else if( res instanceof XmlPersistableResource ) {
                log.debug( "found a xmlpersistable resource: " );
                return new SourcePage( (XmlPersistableResource) res );
            } else {
                log.debug( "found a resource, but is not source: " + res.getClass() );
                return null;
            }
        } else {
            Resource res = next.getResource( host, url );
            return res;
        }
    }

    public static class NewResourceSourcePage extends VfsCommon implements Replaceable {

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
                String xmlName = el.getAttributeValue( "name" );
                if( !xmlName.equals( name ) ) {
                    throw new IllegalArgumentException( "the name in the url does not match that in the xml. xmlname: " + xmlName + " url name: " + name );
                }
                Resource r = (Resource) ReflectionUtils.create( className, parent, name );
                if( r instanceof XmlPersistableResource) {
                    XmlPersistableResource xmlpr = (XmlPersistableResource) r;
                    xmlpr.loadFromXml( el, null );
                    xmlpr.save();
                    commit();
                } else {
                    throw new RuntimeException( "the requested class does not implement: " + XmlPersistableResource.class);
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

        @Override
        public boolean authorise( Request request, Request.Method method, Auth auth ) {
            ClydeAuthoriser authoriser = requestContext().get( ClydeAuthoriser.class );
            return authoriser.authorise( this, request );
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
    }
}
