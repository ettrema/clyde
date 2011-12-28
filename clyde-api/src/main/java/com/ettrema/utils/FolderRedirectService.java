package com.ettrema.utils;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public class FolderRedirectService implements RedirectService {

	private String redirectPage = "index.html";

	@Override
	public String checkRedirect(Resource resource, Request request) {
		// Only redirect on GET
		Method m = request.getMethod();
		if( !m.equals(Method.GET)) { // do not redirect unless its a GET request
			return null;
		}		
		if( request.getHeaders().containsKey("X-Requested-With")) { // Don't redirect on AJAX requests
			String req = request.getHeaders().get("X-Requested-With");
			if( req.equals("XMLHttpRequest")) {
				return null;
			}				
		}
		if (resource instanceof CollectionResource) {
			String path = request.getAbsoluteUrl();
			if (redirectPage.length() > 0) {
				if (!path.endsWith("/")) {
					path = path + "/";
				}
				path = path + redirectPage;
				return path;
			} else {
				// Just check that url ends with trailing slash and redirect if not
				if (!path.endsWith("/")) {
					path = path + "/";
					return path;
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
