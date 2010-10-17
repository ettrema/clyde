package com.bradmcevoy.facebook;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.BufferingOutputStream;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookException;
import com.restfb.Parameter;
import com.restfb.types.Album;
import com.restfb.types.FacebookType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class FacebookResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FacebookResourceFactory.class );
    private final ResourceFactory wrapped;
    private String name = "_sys_facebook";
    private String appId;
    private String appSecret;

    public FacebookResourceFactory( ResourceFactory wrapped ) {
        this.wrapped = wrapped;
    }

    public Resource getResource( String host, String sPath ) {
        log.trace( "getResource" );
        Path path = Path.path( sPath );
        if( path.getName().equals( name ) ) {
            Resource toSend = wrapped.getResource( host, path.getParent().toString() );
            if( toSend == null ) {
                return null;
            } else {
                if( toSend instanceof GetableResource ) {
                    log.trace( "got resource" );
                    return new UploadResource( (GetableResource) toSend, path.getParent() );
                } else {
                    log.warn( "not compatible resource: " + toSend.getClass().getCanonicalName() );
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    /**
     * return the access token
     * 
     * @param request
     * @return
     */
    private Map<String, String> parseRequest( Request request ) throws NoSuchAlgorithmException {
        String cookieName = "fbs_" + appId;
        log.debug( "parseRequest: " + cookieName );
        Cookie cookie = request.getCookie( cookieName );
        if( cookie == null ) {
            log.debug( "no fb cookie" );
            return null;
        }
        String fbCookieValue = cookie.getValue();
        log.debug( "fbCookieValue: " + fbCookieValue );

        //remove first and last double quotes
        String[] pairs = fbCookieValue.split( "\"|&" );
        Arrays.sort( pairs );
        StringBuilder payload = new StringBuilder();
        Map<String, String> map = new HashMap<String, String>();
        for( int i = 0; i < pairs.length; i++ ) {
            String pair = pairs[i];
            if( !( pair.contains( "sig=" ) ) ) {
                payload.append( pair );
            } else {
                System.out.println( "sig: " + pair );
            }
            String[] nv = pair.split( "=" );
            map.put( nv[0], nv[1] );
        }
        payload.append( appSecret );
        log.debug( "payload: " + payload );

        MessageDigest digest = java.security.MessageDigest.getInstance( "MD5" );
        digest.reset();
        digest.update( payload.toString().getBytes() );
        byte[] hash = digest.digest();
        StringBuilder buf = new StringBuilder();

        for( int i = 0; i < hash.length; i++ ) {
            String hex = Integer.toHexString( 0xff & hash[i] );
            if( hex.length() == 1 ) buf.append( '0' );
            buf.append( hex );
        }
        log.debug( "hash: " + buf );
        return map;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId( String appId ) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret( String appSecret ) {
        this.appSecret = appSecret;
    }

    public class UploadResource implements PostableResource {

        private final GetableResource toSend;
        private final Path resourcePath;
        private String accessToken;
        private String result;

        public UploadResource( GetableResource toSend, Path resourcePath ) {
            this.toSend = toSend;
            this.resourcePath = resourcePath;
        }

        public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) throws BadRequestException, NotAuthorizedException, ConflictException {
            log.debug( "processForm" );
            if( accessToken == null ) {
                throw new BadRequestException( this );
            }
            try {
                FacebookClient facebookClient = new DefaultFacebookClient( accessToken );
                String destAlbum = getAlbumName();
                log.debug( "destAlbum: " + destAlbum );
                String destAlbumId = getOrCreateAlbum( facebookClient, destAlbum );
                log.debug( "destAlbumId: " + destAlbumId );
                InputStream content = null;
                try {
                    content = getContent();
                    destAlbumId = destAlbumId + "/photos";
                    FacebookType publishPhotoResponse = facebookClient.publish( destAlbumId, FacebookType.class, content, Parameter.with( "message", "Published by shmego.com" ) );
                    log.debug( "publishjed ok: " + publishPhotoResponse.getId() );
                } catch( IOException ex ) {
                    throw new RuntimeException( ex );
                } finally {
                    IOUtils.closeQuietly( content );
                }
                result = "ok";
            } catch( FacebookException ex ) {
                log.error( "exception loading: " + resourcePath, ex );
                result = "err";
            }
            return null;
        }

        public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
            String s;
            if( result == null ) {
                // not a POST
                if( accessToken != null ) {
                    s = "loggedIn";
                } else {
                    s = "notLoggedIn";
                }
            } else {
                s = result;
            }
            out.write( s.getBytes() );
        }

        public Long getMaxAgeSeconds( Auth auth ) {
            return null;
        }

        public String getContentType( String accepts ) {
            return "text/plain";
        }

        public Long getContentLength() {
            return null;
        }

        public String getUniqueId() {
            return null;
        }

        public String getName() {
            return name;
        }

        public Object authenticate( String user, String password ) {
            return toSend.authenticate( user, password );
        }

        public boolean authorise( Request request, Method method, Auth auth ) {
            return toSend.authorise( request, method, auth );
        }

        public String getRealm() {
            return toSend.getRealm();
        }

        public Date getModifiedDate() {
            return null;
        }

        public String checkRedirect( Request request ) {
            try {
                Map<String, String> vals = parseRequest( request );
                if( vals != null ) {
                    accessToken = vals.get( "access_token" );
                }
                return null;
            } catch( NoSuchAlgorithmException ex ) {
                throw new RuntimeException( ex );
            }
        }

        private String getAlbumName() {
            Path p = resourcePath.getParent();
            if( p.getName().startsWith( "_sys")) p = p.getParent();
            return p.getName();
        }

        private String getOrCreateAlbum( FacebookClient facebookClient, String destAlbum ) throws FacebookException {
            Connection<Album> myAlbums = facebookClient.fetchConnection( "me/albums", Album.class );
            for( Album a : myAlbums.getData() ) {
                if( a.getName().equals( destAlbum ) ) {
                    log.debug( "got album: " + a.getId() );
                    return a.getId();
                }
            }
            log.debug( "does not exist, create it" );
            FacebookType resp = facebookClient.publish( "/me/albums", FacebookType.class, Parameter.with( "name", destAlbum ) );
            log.debug( "created: " + resp.getId() );
            return resp.getId();
        }

        private InputStream getContent() throws IOException, NotAuthorizedException, BadRequestException {
            BufferingOutputStream out = null;
            try {
                out = new BufferingOutputStream( 50000 );
                toSend.sendContent( out, null, null, null );
            } finally {
                IOUtils.closeQuietly( out );
            }
            log.debug( "data: " + out.getSize() );
            return out.getInputStream();
        }
    }
}
