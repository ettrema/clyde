package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.vfs.VfsCommon;
import java.util.Date;

public abstract class FckCommon extends VfsCommon implements PostableResource, DigestResource {

    protected Path url;
    protected final Host host;

    FckCommon( Host host, Path url ) {
        this.host = host;
        this.url = url;
    }

    @Override
    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    @Override
    public String getName() {
        return "connector.html";
    }

    @Override
    public Object authenticate( String user, String password ) {
        return host.authenticate( user, password );
    }

    @Override
    public Object authenticate( DigestResponse dr ) {
        return host.authenticate( dr );
    }

    @Override
    public boolean authorise( Request request, Request.Method method, Auth auth ) {
        return auth != null;
    }

    @Override
    public String getRealm() {
        return host.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String checkRedirect( Request request ) {
        return null;
    }
}
