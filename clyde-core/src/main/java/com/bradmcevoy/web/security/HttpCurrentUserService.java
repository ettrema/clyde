package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.web.IUser;

/**
 * Just
 *
 * @author brad
 */
public class HttpCurrentUserService implements CurrentUserService{

    public IUser getSecurityContextUser() {
        Auth auth = HttpManager.request().getAuthorization();
        if( auth == null ) return null;
        return (IUser) auth.getTag();
    }

    public IUser getOnBehalfOf() {
        return getSecurityContextUser();
    }

}
