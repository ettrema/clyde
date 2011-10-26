package com.ettrema.web.component;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.ettrema.web.Component;

/**
 *
 * @author brad
 */
public interface SecureComponent extends Component {
    public boolean authorise(Request request, Request.Method method, Auth auth);

}
