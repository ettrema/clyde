package com.ettrema.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;

/**
 *  Checks the isSecure property on the Folder, if the resource is a folder
 * 
 * For compatibility, should be called after checking for a security component
 *
 * @author brad
 */
public class FolderSecureReadAuthoriser implements ClydeAuthoriser{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FolderSecureReadAuthoriser.class );

    @Override
    public String getName() {
        return FolderSecureReadAuthoriser.class.getCanonicalName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method, Auth auth ) {
        log.debug( "authorise");
        if( resource instanceof Folder ){
            log.debug( "is a folder");
            Folder f = (Folder) resource;
            if( f.isSecureRead()) {
                log.debug( "is secure read");
                return ( auth == null );
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
