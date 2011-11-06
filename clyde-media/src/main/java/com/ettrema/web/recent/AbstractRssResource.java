package com.ettrema.web.recent;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.context.RequestContext;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.IUser;
import com.ettrema.web.security.ClydeAuthenticator;
import com.ettrema.web.security.ClydeAuthoriser;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author bradm
 */
public abstract class AbstractRssResource implements GetableResource, DigestResource {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractRssResource.class);
	public static final String PATTERN_RESPONSE_HEADER = "E, dd MMM yyyy HH:mm:ss Z"; // Tue, 29 Jun 2010 10:37:14 +1200
	private final String name;
	protected final Folder folder;
	
	public AbstractRssResource(Folder folder, String name) {
		this.name = name;
		this.folder = folder;
	}	

	
	
	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return null;
	}

	@Override
	public String getContentType(String accepts) {
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
	public boolean authorise(Request request, Method method, Auth auth) {
		ClydeAuthoriser authoriser = requestContext().get(ClydeAuthoriser.class);
		return authoriser.authorise(folder, request, method, auth);
	}

	@Override
	public String getRealm() {
		return getHost().getName();
	}

	@Override
	public Date getModifiedDate() {
		return null;
	}

	@Override
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

	@Override
	public boolean isDigestAllowed() {
		return folder.isDigestAllowed();
	}

	protected RequestContext requestContext() {
		return RequestContext.getCurrent();
	}

	public Host getHost() {
		return folder.getHost();
	}

	public String formatDate(Date date) {
		DateFormat df = new SimpleDateFormat(PATTERN_RESPONSE_HEADER);
		return df.format(date);
	}

	protected String fixHtml(String s) {
		return s.replace("&nbsp", "#160;");
	}	
}
