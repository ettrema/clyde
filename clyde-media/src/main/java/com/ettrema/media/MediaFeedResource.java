package com.ettrema.media;

import com.ettrema.media.dao.MediaLogCollector;
import com.bradmcevoy.common.Path;
import com.ettrema.media.MediaLogService.MediaType;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.IUser;
import com.ettrema.web.security.ClydeAuthenticator;
import com.ettrema.web.security.ClydeAuthoriser;
import com.ettrema.context.RequestContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
    private final MediaFeedLinkGenerator linkGenerator;
    private final MediaLogService mediaLogService;
    private final Folder folder;
    private final Long cacheSeconds;
    private final String basePath;
    private int itemCount;

    public MediaFeedResource( MediaLogService logService, MediaFeedLinkGenerator linkGenerator, String name, Folder folder, Long cacheSeconds, String basePath ) {
        this.name = name;
        this.mediaLogService = logService;
        this.linkGenerator = linkGenerator;
        this.folder = folder;
        this.cacheSeconds = cacheSeconds;
        this.basePath = basePath;
    }

	@Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        int page = getIntParam( params, "page" );
		boolean isAlbum = getBoolParam(params,"album");
        final int skip = page < 10 ? page * 2 : 20; //
        log.warn( "sendContent: page:" + page );

        XmlWriter writer = new XmlWriter( out );
        writer.writeXMLHeader();
        String hostUrl = basePath;

        Host host = folder.getHost();

		// TODO: if isAlbum load albums. And do content type check for JSON
		
        final Element elChannel = writer.begin( "rss" ).writeAtt( "version", "2.0" ).writeAtt( "xmlns:media", "http://search.yahoo.com/mrss/" ).writeAtt( "xmlns:atom", "http://www.w3.org/2005/Atom" ).begin( "channel" ).prop( "title", host.getName() ).prop( "link", hostUrl );

        String folderPath = toFolderPath( HttpManager.request().getAbsolutePath() );

        int numResults = mediaLogService.search( host.getNameNodeId(), folderPath, page, new MediaLogCollector() {

			@Override
            public void onResult( UUID nameId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaType type ) {
                log.debug( "onResult: " + type );
                if( itemCount++ >= skip ) {
                    itemCount = 0;
                    Path path = Path.path( mainContentPath );
                    String title = path.getParent().getParent().getName() + " " + getTitleFromDate( dateTaken );
                    if( type == MediaType.IMAGE ) {
                        appendImage( elChannel, title, dateTaken, mainContentPath, thumbPath );
                    } else if( type == MediaType.VIDEO ) {
                        appendVideo( elChannel, title, dateTaken, mainContentPath, thumbPath );
                    } else {
                        log.trace( "unknown type: " + type );
                    }
                }
            }
        } );

        if( page > 0 ) {
            appendPageLink( "previous", elChannel, page - 1 );
        }
        if( numResults >= mediaLogService.getPageSize() ) {
            appendPageLink( "next", elChannel, page + 1 );
        }

        elChannel.close().close();

        writer.flush();
    }

	

    private void appendPageLink( String rel, Element elChannel, int page ) {
        log.trace( "appendPage: " + rel + " - " + page );
        String feedHref = basePath + "/" + name + "?page=" + page;
        elChannel.begin( "atom:link" ).writeAtt( "rel", rel ).writeAtt( "href", feedHref ).close();

    }

    private void appendImage( Element elChannel, String title, Date dateTaken, String mainContentPath, String thumbPath ) {
        String hostUrl = "";
        String thumbUrl = hostUrl + thumbPath;
        String contentUrl = hostUrl + mainContentPath;
        String link = mainContentPath;
        if( linkGenerator != null ) {
            link = linkGenerator.generateLink( MediaType.IMAGE, mainContentPath );
        }
        Element elImg = elChannel.begin( "item" ).prop( "title", title ).prop( "media:description", title ).prop( "link", link );
        elImg.begin( "media:thumbnail" ).writeAtt( "url", thumbUrl ).close();
        elImg.begin( "media:content" ).writeAtt( "url", contentUrl ).close();
        elImg.close( true );
    }

    private void appendVideo( Element elChannel, String title, Date dateTaken, String mainContentPath, String thumbPath ) {
        String hostUrl = "";
        String thumbUrl = hostUrl + thumbPath;
        String contentUrl = hostUrl + mainContentPath;
        String link = mainContentPath;
        if( linkGenerator != null ) {
            link = linkGenerator.generateLink( MediaType.VIDEO, mainContentPath );
        }
        Element elImg = elChannel.begin( "item" ).prop( "title", title ).prop( "media:description", title ).prop( "link", link );
        elImg.begin( "media:thumbnail" ).writeAtt( "url", thumbUrl ).close();
        elImg.begin( "media:content" ).writeAtt( "url", contentUrl ).writeAtt( "type", "video/x-flv" ).close();
        elImg.close( true );
    }

	@Override
    public Long getMaxAgeSeconds( Auth auth ) {
        return cacheSeconds;
    }

	@Override
    public String getContentType( String accepts ) {
        return "application/rss+xml ";
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
        return name;
    }

	@Override
    public boolean authorise( Request request, Method method, Auth auth ) {
        ClydeAuthoriser authoriser = requestContext().get( ClydeAuthoriser.class );
        return authoriser.authorise( folder, request, method, auth );
    }

	@Override
    public String getRealm() {
        return folder.getName();
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
    public IUser authenticate( String user, String password ) {
        ClydeAuthenticator authenticator = requestContext().get( ClydeAuthenticator.class );
        IUser o = authenticator.authenticate( folder, user, password );
        if( o == null ) {
            log.warn( "authentication failed by: " + authenticator.getClass() );
        }
        return o;
    }

    @Override
    public Object authenticate( DigestResponse digestRequest ) {
        ClydeAuthenticator authenticator = requestContext().get( ClydeAuthenticator.class );
        Object o = authenticator.authenticate( folder, digestRequest );
        if( o == null ) {
            log.warn( "authentication failed by: " + authenticator.getClass() );
        }
        return o;
    }

	@Override
    public boolean isDigestAllowed() {
        return folder.isDigestAllowed();
    }



    protected RequestContext requestContext() {
        return RequestContext.getCurrent();
    }

    private String toFolderPath( String basePath ) {
        int pos = basePath.lastIndexOf( "/" );
        return basePath.substring( 0, pos );
    }

    private String getTitleFromDate( Date dateTaken ) {
        Calendar cal = Calendar.getInstance();
        cal.setTime( dateTaken );
        String s = cal.getDisplayName( Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH ) + " " + cal.get( Calendar.YEAR );
        return s;
    }

    private int getIntParam( Map<String, String> params, String name ) throws NumberFormatException {
        String sPage = params.get( name );
        int page;
        if( StringUtils.isEmpty( sPage ) ) {
            page = 0;
        } else {
            page = Integer.parseInt( sPage );
        }
        return page;
    }
	
	private boolean getBoolParam(Map<String, String> params, String string) {
        String s = params.get( name );
        boolean b;
        if( StringUtils.isEmpty( s ) ) {
            b = false;
        } else {
            b = Boolean.parseBoolean(s);
        }
        return b;
		
	}	
}
