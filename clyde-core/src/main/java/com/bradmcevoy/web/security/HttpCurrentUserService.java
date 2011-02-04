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

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HttpCurrentUserService.class);

    public IUser getSecurityContextUser() {
        if (HttpManager.request() == null) {
            return null;
        }
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

    public void setOnBehalfOf(IUser user) {
        if (log.isTraceEnabled()) {
            if (user == null) {
                log.trace("setOnBehalfOf: " + null);
            } else {
                log.trace("setOnBehalfOf: " + user.getName());
            }
        }
        Auth auth = new Auth(user.getName(), user);
        HttpManager.request().setAuthorization(auth);
    }
}
