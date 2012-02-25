package com.ettrema.utils;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;

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
        if (!m.equals(Method.GET)) { // do not redirect unless its a GET request
            return null;
        }
        if (request.getHeaders().containsKey("X-Requested-With")) { // Don't redirect on AJAX requests
            String req = request.getHeaders().get("X-Requested-With");
            if (req.equals("XMLHttpRequest")) {
                return null;
            }
        }
        if (resource instanceof Folder) {
            Folder folder = (Folder) resource;
            boolean isGetable = folder.isGetable();
            String path = request.getAbsoluteUrl();
            if( !isGetable && (redirectPage != null && redirectPage.length() > 0)) {
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
