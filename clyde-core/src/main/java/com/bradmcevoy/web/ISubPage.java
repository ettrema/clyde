package com.bradmcevoy.web;

import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public interface ISubPage extends Templatable{
    /**
     * subpages may require a logged in user, despite the folder's permissions
     *
     * @return
     */
    boolean isSecure();

    Resource getChildResource( String childName );

    String getContentType( String accepts );
}
