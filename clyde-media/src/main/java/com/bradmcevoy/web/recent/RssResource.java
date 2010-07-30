package com.bradmcevoy.web.recent;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.ImageFile.ImageData;
import com.bradmcevoy.web.Page;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.ClydeAuthenticator;
import com.bradmcevoy.web.security.ClydeAuthoriser;
import com.ettrema.context.RequestContext;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implements an RSS feed. Can be used on a Recent folder because RecentResource's
 * are converted to their targets
 *
 * Has specific support for images and pages
 *
 */
public class RssResource implements GetableResource, DigestResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RssResource.class);

    public static final String PATTERN_RESPONSE_HEADER = "E, dd MMM yyyy HH:mm:ss Z"; // Tue, 29 Jun 2010 10:37:14 +1200

    private final String name;
    private final Folder folder;

    public RssResource(Folder folder, String name) {
        this.name = name;
        this.folder = folder;
    }

    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        String where = params.get("where");
        boolean includeImages = true;
        boolean includePages = true;
        if (where != null && where.length() > 0) {
            if (where.equals("image")) {
                log.debug("not include pages");
                includePages = false;
            } else if (where.equals("page")) {
                log.debug("not include images");
                includeImages = false;
            } else {
                log.warn("unknown where: " + where);
            }
        }

        List<BaseResource> list = getResources();
        XmlWriter writer = new XmlWriter(out);
        writer.writeXMLHeader();
        Element elChannel = writer.begin("rss").writeAtt("version", "2.0").begin("channel").prop("title", folder.getTitle()).prop("link", folder.getHref());

        for (BaseResource r : list) {
            if (r instanceof RecentResource) {
                RecentResource rr = (RecentResource) r;
                appendBaseResource(rr.getTargetResource(), elChannel, includeImages, includePages);
            } else {
                appendBaseResource(r, elChannel, includeImages, includePages);
            }
        }
        elChannel.close().close();

        writer.flush();
    }

    private void appendBaseResource(BaseResource r, Element elChannel, boolean includeImages, boolean includePages) {
        if (r == null) {
            return;
        }

        if (r instanceof ImageFile && includeImages) {
            ImageFile img = (ImageFile) r;
            appendImage(img, elChannel);
        } else if (r instanceof Page && includePages) {
            Page page = (Page) r;
            appendPage(page, elChannel);
        }

    }

    private void appendPage(Page page, Element elChannel) {
        User creator = page.getCreator();
        String author = null;
        if( creator != null ) {
            author = creator.getName();
        }
        Element elItem = elChannel.begin("item")
                .prop("title", page.getTitle())
                .prop("description", page.getBrief())
                .prop("link", page.getHref())
                .prop("pubDate", formatDate(page.getModifiedDate()));

        if( author != null ) {
            elItem.prop("author",author);
        }

        elItem.close();

    }

    private void appendImage(ImageFile target, Element elChannel) {
        ImageData data = target.imageData(false);
        Element elImg = elChannel.begin("image")
                .prop("title", target.getTitle())
                .prop("link", target.getParent().getHref())
                .prop("pubDate", formatDate(target.getModifiedDate()))
                .prop("url", target.getHref());
        if (data != null) {
            elImg.prop("width", data.getWidth());
            elImg.prop("height", data.getHeight());
        }
        elImg.close(true);
    }

    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    public String getContentType(String accepts) {
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

    public boolean authorise(Request request, Method method, Auth auth) {
        ClydeAuthoriser authoriser = requestContext().get(ClydeAuthoriser.class);
        return authoriser.authorise(folder, request, method);
    }

    public String getRealm() {
        return getHost().getName();
    }

    public Date getModifiedDate() {
        return null;
    }

    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public User authenticate(String user, String password) {
        ClydeAuthenticator authenticator = requestContext().get(ClydeAuthenticator.class);
        User o = authenticator.authenticate(folder, user, password);
        if (o == null) {
            log.warn("authentication failed by: " + authenticator.getClass());
        }
        return o;
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        ClydeAuthenticator authenticator = requestContext().get(ClydeAuthenticator.class);
        Object o = authenticator.authenticate(folder, digestRequest);
        if (o == null) {
            log.warn("authentication failed by: " + authenticator.getClass());
        }
        return o;
    }

    protected RequestContext requestContext() {
        return RequestContext.getCurrent();
    }

    public Host getHost() {
        return folder.getHost();
    }

    private List<BaseResource> getResources() {
        List<BaseResource> list = new ArrayList<BaseResource>();
        for (Resource r : folder.getChildren()) {
            if (r instanceof BaseResource) {
                list.add((BaseResource) r);
            }
        }
        return list;
    }


    public String formatDate(Date date) {
        DateFormat df = new SimpleDateFormat(PATTERN_RESPONSE_HEADER);
        return df.format(date);
    }



}
