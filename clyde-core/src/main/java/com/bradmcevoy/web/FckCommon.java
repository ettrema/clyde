package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.vfs.VfsCommon;
import java.util.Date;

public abstract  class FckCommon extends VfsCommon implements PostableResource {
    protected  Path url;

    protected  final Host host;
    
    FckCommon(Host host, Path url)  {
        this.host = host;
        this.url = url;
    }

// Boring accessors and stuff
        
    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getName() {
        return "connector.html";
    }

    @Override
    public Object authenticate(String user, String password) {
        return true;
    }

    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
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
    public String checkRedirect(Request request) {
        return null;
    }    
}
