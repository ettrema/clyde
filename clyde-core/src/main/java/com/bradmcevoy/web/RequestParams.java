package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import java.util.HashMap;
import java.util.Map;

public class RequestParams {

    private static final ThreadLocal<RequestParams> tlCurrent = new ThreadLocal<RequestParams>();
    
    public static RequestParams current() {
        return tlCurrent.get();
    }
    
    public static void setCurrent(RequestParams rp) {
        tlCurrent.set(rp);
    }

    public final Resource resource;
    public final Request request;
    public final String href;
    public final Map<String, String> parameters;
    public final Map<String, FileItem> files;
    public final Map<String, Object> attributes;
    public final Method method;

    public RequestParams(Resource resource, Request request, Map<String, String> parameters, Map<String, FileItem> files) {
        this.request = request;
        this.resource = resource;
        this.href = request.getAbsolutePath();
        this.method = request.getMethod();
        this.parameters = parameters;
        this.files = files;
        attributes = new HashMap<String, Object>();

    }


    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Map<String, FileItem> getFiles() {
        return files;
    }

    public Auth getAuth() {
        return request.getAuthorization();
    }

    public Request getRequest() {
        return request;
    }

    

    public String getHref() {
        return href;
    }

    public Method getMethod() {
        return method;
    }

    public String getAuthReason() {
        String s = (String) request.getAttributes().get("authReason");
        return s;
    }

    public Boolean isLoginResult() {
        Boolean b = (Boolean) request.getAttributes().get("loginResult");
        return b;
    }

    public boolean isDidLogin() {
        return isLoginResult() != null;
    }

    public boolean isLoginRequired() {
        return "required".equals( getAuthReason());
    }

    public boolean isNotPermitted() {
        return "notPermitted".equals(getAuthReason());
    }

    public Path getPath() {
        return Path.path(href);
    }

    public Resource getResource() {
        return resource;
    }
	
	public boolean hasParam(String name) {
		String s = getParameters().get(name);
		return s != null && s.length() > 0;
	}
}
