package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.web.IUser;

/**
 * Just
 *
 * @author brad
 */
public class HttpCurrentUserService implements CurrentUserService {

    public IUser getSecurityContextUser() {
        Auth auth = HttpManager.request().getAuthorization();
        if (auth == null) {
            return null;
        }
        Object tag = auth.getTag();
        if (tag instanceof IUser) {
            return (IUser) tag;
        } else {
            if (tag == null) {
                return null;
            } else {
                throw new RuntimeException("auth.tag is not a IUser. Is a: " + tag.getClass().getCanonicalName());
            }
        }
    }

    public IUser getOnBehalfOf() {
        return getSecurityContextUser();
    }
}
