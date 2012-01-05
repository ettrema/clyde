package com.ettrema.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

public class ErrorPage implements GetableResource, PostableResource, DigestResource {

    final String href;
    final String name;
    final Resource parent;
    final String errorMessage;

    public ErrorPage(String href, String name, Folder folder) {
        this(href, name, folder, null);
    }

    public ErrorPage(String href, String name, Resource parent, String errorMessage) {
        this.href = href;
        this.name = name;
        this.parent = parent;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        out.write("<html><body><h1>Error</h1>".getBytes());
        doBody(out);
        out.write("</body></html>".getBytes());
    }

    protected void doBody(OutputStream out) throws IOException {
        out.write(errorMessage.getBytes());
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object authenticate(String user, String password) {
        return true;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return true;
    }

    @Override
    public String getRealm() {
        return parent.getRealm();
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
    public String getContentType(String accepts) {
        return "text/html";
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) {
        return null;
    }

    @Override
    public Object authenticate( DigestResponse digestRequest ) {
        return digestRequest.getUser();
    }

    public boolean isDigestAllowed() {
        return true;
    }


}
