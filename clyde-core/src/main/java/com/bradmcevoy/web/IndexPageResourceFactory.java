package com.bradmcevoy.web;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * Implements a mapping from folders onto index pages for GET requests which have
 * a url ending with a slash
 * 
 * This allows content to be delivered on shorter urls, but which is still correct
 * in terms of relative paths. Eg the following urls would give the same result:
 * 
 * /files/index.html
 * /files/
 * 
 * Note that we cannot do this for a url without a trailing slash like this:
 * /files
 * 
 * Because, without a trailing slash, links in the content would resolve to different
 * addresses, resulting in broken links depending on whether the user put a slash
 * at the end of the url or not. For example, if we generate content at /files
 * and have a link to page1.html that would resolve to /page1.html. But if the user
 * put a trailing slash on, and requested /files/, then it would resolve to /files/page1.html
 * 
 * So for this feature to work there must be a seperate mechanism to ensure that collection
 * url's are accessed consistently - ie with a redirect if there's a missing slash
 *
 * @author bradm
 */
public class IndexPageResourceFactory implements ResourceFactory {

	private final ResourceFactory wrapped;

	public IndexPageResourceFactory(ResourceFactory wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public Resource getResource(String host, String url) {
		System.out.println("inex page get: " + url);
		Resource r = wrapped.getResource(host, url);
		if (r != null) {
			System.out.println("indexpage: r: " + r.getClass());
			System.out.println("a1: " + (r instanceof CollectionResource));
			System.out.println("a2: " + ( r instanceof Templatable));
			if (r instanceof CollectionResource && r instanceof Templatable ) {
				// if its a GET on a collection, and the url correctly ends with a slash
				// then look for an index file and return that if it exists
				if (url.endsWith("/") && isGetOrPost()) {
					CollectionResource cr = (CollectionResource) r;
					Resource index = cr.child("index.html");
					if (index != null) {
						System.out.println("mapping request onto index page: " + index.getClass());
						if (index instanceof ISubPage) {
							ISubPage sp = (ISubPage) index;
							index = new WrappedSubPage(sp, (Templatable)cr);													
						}
						return index;
					}
				}
			}
		}
		System.out.println("index page rf: " + r);
		return r;
	}

	private boolean isGetOrPost() {
		Method m = HttpManager.request().getMethod();
		return m.equals(Request.Method.GET) || m.equals(Request.Method.POST);
	}
}
