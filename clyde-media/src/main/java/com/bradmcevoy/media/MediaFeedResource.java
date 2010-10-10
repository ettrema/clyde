package com.bradmcevoy.media;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.media.MediaLogService.MediaType;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.ClydeAuthenticator;
import com.bradmcevoy.web.security.ClydeAuthoriser;
import com.ettrema.context.RequestContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;

/**
 * Implements an RSS feed. Can be used on a Recent folder because RecentResource's
 * are converted to their targets
 *
 * Has specific support for images and pages
 *
 */
public class MediaFeedResource implements GetableResource, DigestResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MediaFeedResource.class );
    public static final String PATTERN_RESPONSE_HEADER = "E, dd MMM yyyy HH:mm:ss Z"; // Tue, 29 Jun 2010 10:37:14 +1200
    private final String name;
    private final MediaLogService logService;
    private final Host host;
    private final Long cacheSeconds;
    private final String basePath;

    public MediaFeedResource( MediaLogService logService, String name, Host host, Long cacheSeconds, String basePath ) {
        this.name = name;
        this.logService = logService;
        this.host = host;
        this.cacheSeconds = cacheSeconds;
        this.basePath = basePath;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        String sPage = params.get( "page" );
        int page;
        if( StringUtils.isEmpty( sPage ) ) {
            page = 0;
        } else {
            page = Integer.parseInt( sPage );
        }
        log.warn( "sendContent: page:" + page );

        XmlWriter writer = new XmlWriter( out );
        writer.writeXMLHeader();
        String hostUrl = hostUrl();

        final Element elChannel = writer.begin( "rss" ).writeAtt( "version", "2.0" ).writeAtt( "xmlns:media", "http://search.yahoo.com/mrss/" ).writeAtt( "xmlns:atom", "http://www.w3.org/2005/Atom" ).begin( "channel" ).prop( "title", host.getName() ).prop( "link", hostUrl );

        int numResults = logService.search( host.getNameNodeId(), page, new MediaLogService.ResultCollector() {

            public void onResult( UUID nameId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaType type ) {
                log.debug( "onResult: " + type );
                Path path = Path.path( mainContentPath );
                if( type == MediaType.IMAGE ) {
                    appendImage( elChannel, path.getName(), dateTaken, mainContentPath, thumbPath );
                }
            }
        } );

        if( page > 0 ) {
            appendPageLink("previous", elChannel, page-1);
        }
        if( numResults >= logService.getPageSize()) {
            appendPageLink("next",elChannel, page+1);
        }

        elChannel.close().close();

        writer.flush();
    }

    private void appendPageLink( String rel, Element elChannel, int page ) {
        log.trace("appendPage: " + rel + " - " + page);
        String feedHref = basePath + "/" + name + "?page=" + page;
        elChannel.begin( "atom:link" )
            .writeAtt( "rel", rel)
            .writeAtt( "href", feedHref)
            .close();

    }

    private String hostUrl() {
        return basePath;
    }

    private void appendImage( Element elChannel, String title, Date dateTaken, String mainContentPath, String thumbPath ) {
        String hostUrl = hostUrl();
        String thumbUrl = hostUrl + thumbPath;
        String contentUrl = hostUrl + mainContentPath;
        Element elImg = elChannel.begin( "item" ).prop( "title", title ).prop( "media:description", title ).prop( "link", mainContentPath );
        elImg.begin( "media:thumbnail" ).writeAtt( "url", thumbUrl ).close();
        elImg.begin( "media:content" ).writeAtt( "url", contentUrl ).close();
        elImg.close( true );
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return cacheSeconds;
    }

    public String getContentType( String accepts ) {
        return "application/rss+xml ";
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

    public boolean authorise( Request request, Method method, Auth auth ) {
        ClydeAuthoriser authoriser = requestContext().get( ClydeAuthoriser.class );
        return authoriser.authorise( host, request, method, auth );
    }

    public String getRealm() {
        return host.getName();
    }

    public Date getModifiedDate() {
        return null;
    }

    public String checkRedirect( Request request ) {
        return null;
    }

    @Override
    public User authenticate( String user, String password ) {
        ClydeAuthenticator authenticator = requestContext().get( ClydeAuthenticator.class );
        User o = authenticator.authenticate( host, user, password );
        if( o == null ) {
            log.warn( "authentication failed by: " + authenticator.getClass() );
        }
        return o;
    }

    @Override
    public Object authenticate( DigestResponse digestRequest ) {
        ClydeAuthenticator authenticator = requestContext().get( ClydeAuthenticator.class );
        Object o = authenticator.authenticate( host, digestRequest );
        if( o == null ) {
            log.warn( "authentication failed by: " + authenticator.getClass() );
        }
        return o;
    }

    protected RequestContext requestContext() {
        return RequestContext.getCurrent();
    }


}
