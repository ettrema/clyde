package com.bradmcevoy.web.component;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.web.Component;

/**
 *
 * @author brad
 */
public interface SecureComponent extends Component {
    public boolean authorise(Request request, Request.Method method, Auth auth);

}
