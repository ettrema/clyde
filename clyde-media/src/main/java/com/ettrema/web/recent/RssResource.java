package com.bradmcevoy.web.recent;

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
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BaseResourceList;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.ImageFile.ImageData;
import com.bradmcevoy.web.Page;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.ClydeAuthenticator;
import com.bradmcevoy.web.security.ClydeAuthoriser;
import com.ettrema.context.RequestContext;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
		log.trace("sendContent1");
		String where = params.get("where");
		boolean includeImages = true;
		boolean includePages = true;
		boolean includeBinaries = true;
		if (where != null && where.length() > 0) {
			if (where.equals("image")) {
				log.debug("not include pages");
				includePages = false;
				includeBinaries = false;
			} else if (where.equals("page")) {
				log.debug("not include images");
				includeImages = false;
				includeBinaries = false;
			} else {
				log.warn("unknown where: " + where);
			}
		}

		BaseResourceList list = getResources();
		list = list.getSortByModifiedDate();
		XmlWriter writer = new XmlWriter(out);
		writer.writeXMLHeader();
		Element elChannel = writer.begin("rss").writeAtt("version", "2.0").begin("channel").prop("title", folder.getTitle()).prop("link", folder.getHref());
		if (log.isTraceEnabled()) {
			log.trace("sendContent: resources: " + list.size());
		}
		for (Templatable r : list) {
			if (r instanceof RecentResource) {
				RecentResource rr = (RecentResource) r;
				appendBaseResource(rr.getTargetResource(), elChannel, includeImages, includePages, includeBinaries);
			} else if(r instanceof BaseResource) {
				appendBaseResource((BaseResource)r, elChannel, includeImages, includePages, includeBinaries);
			}
		}
		elChannel.close().close();

		writer.flush();
	}

	private void appendBaseResource(BaseResource r, Element elChannel, boolean includeImages, boolean includePages, boolean includeBinaries) {
		if (r == null) {
			return;
		}

		if (r instanceof ImageFile && includeImages) {
			ImageFile img = (ImageFile) r;
			appendImage(img, elChannel);
		} else if (r instanceof Page && includePages) {
			Page page = (Page) r;
			appendPage(page, elChannel);
		} else if( includeBinaries) {
			appendBinaryFile(r, elChannel);
		} else {
			log.trace("Not including: " + r.getClass());
		}

	}

	private void appendPage(Page page, Element elChannel) {
		User creator = page.getCreator();
		String author = null;
		if (creator != null) {
			author = creator.getName();
		}
		Element elItem = elChannel.begin("item").prop("title", page.getTitle()).prop("description", fixHtml(page.getBrief())).prop("link", page.getHref()).prop("pubDate", formatDate(page.getModifiedDate()));

		if (author != null) {
			elItem.prop("author", author);
		}
		elItem.close();
	}
	
	private void appendBinaryFile(BaseResource page, Element elChannel) {
		User creator = page.getCreator();
		String author = null;
		if (creator != null) {
			author = creator.getName();
		}
		Element elItem = elChannel.begin("item").prop("title", page.getName()).prop("link", page.getHref()).prop("pubDate", formatDate(page.getModifiedDate()));

		if (author != null) {
			elItem.prop("author", author);
		}
		elItem.close();
	}	

	private void appendImage(ImageFile target, Element elChannel) {
		ImageData data = target.imageData(false);
		Element elImg = elChannel.begin("image").prop("title", target.getTitle()).prop("link", target.getParent().getHref()).prop("pubDate", formatDate(target.getModifiedDate())).prop("url", target.getHref());
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
		return authoriser.authorise(folder, request, method, auth);
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
	public IUser authenticate(String user, String password) {
		ClydeAuthenticator authenticator = requestContext().get(ClydeAuthenticator.class);
		IUser o = authenticator.authenticate(folder, user, password);
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

	public boolean isDigestAllowed() {
		return folder.isDigestAllowed();
	}

	protected RequestContext requestContext() {
		return RequestContext.getCurrent();
	}

	public Host getHost() {
		return folder.getHost();
	}

	private BaseResourceList getResources() {
		return (BaseResourceList) folder.getPagesRecursive();
	}

	public String formatDate(Date date) {
		DateFormat df = new SimpleDateFormat(PATTERN_RESPONSE_HEADER);
		return df.format(date);
	}
	
	private String fixHtml(String s) {
		return s.replace("&nbsp", "#160;");
	}
}
