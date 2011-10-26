package com.ettrema.utils;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public class FolderRedirectService implements RedirectService {

	private String redirectPage = "index.html";

	@Override
	public String checkRedirect(Resource resource, Request request) {
		if (resource instanceof CollectionResource) {
			String s = request.getAbsoluteUrl();
			if (redirectPage.length() > 0) {
				if (!s.endsWith("/")) {
					s = s + "/";
				}
				s = s + redirectPage;
				return s;
			} else {
				// Just check that url ends with trailing slash and redirect if not
				if (!s.endsWith("/")) {
					s = s + "/";
					return s;
				} else {
					// if redirect is blank it means we want the folder to handle the GET
					// Note that there must be some mechanism to generate content from
					// a collection. At time of writing the ExistingResourceFactory will 
					// look for an index page and swap it in for a GET to a collection url
					// if the url has a trailing slash
					return null;
				}	
			}
		} else {
			return null;
		}
	}

	public String getRedirectPage() {
		return redirectPage;
	}

	public void setRedirectPage(String redirectPage) {
		this.redirectPage = redirectPage;
	}
}
