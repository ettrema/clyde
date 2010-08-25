package com.bradmcevoy.utils;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Template;
import java.util.List;

/**
 *
 * @author brad
 */
public class FolderRedirectService implements RedirectService {

    private String redirectPage = "index.html";

    public String checkRedirect( Resource resource, Request request ) {
        if( resource instanceof Folder ) {
            Folder folder = (Folder) resource;
            String s = request.getAbsoluteUrl();
            if( !s.endsWith( "/" ) ) {
                s = s + "/";
            }
            s = s + redirectPage;

            // if logged in and page doesnt exist, go to new page
            Resource r = folder.child( redirectPage );
            if( r == null && request.getAuthorization() != null ) {
                s = s + ".new";
            }
            return s;
        } else {
            return null;
        }
    }

    public String getRedirectPage() {
        return redirectPage;
    }

    public void setRedirectPage( String redirectPage ) {
        this.redirectPage = redirectPage;
    }
}
