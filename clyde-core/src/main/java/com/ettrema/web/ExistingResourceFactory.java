package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.utils.LogUtils;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class ExistingResourceFactory extends CommonResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExistingResourceFactory.class);

    public ExistingResourceFactory(HostFinder hostFinder) {
        super(hostFinder);
    }

    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
        String sPath = url;
        Path path = Path.path(sPath);

        Resource r = findPage(host, path);

        if (r != null) {
            LogUtils.trace(log, "getResource: found", host, url, r.getClass());
        } else {
            LogUtils.trace(log, "getResource: not found", host, url);
        }
        return r;
    }

    public Resource findPage(String host, Path path) throws NotAuthorizedException, BadRequestException {
        if (host != null && host.contains(":")) {
            host = host.substring(0, host.indexOf(":"));
        }
        Host theHost = getHost(host);
        if (theHost == null) {
            log.info("host name not found: " + host);
            return new HostNotFoundResource(host);
        }
        Resource r = findChild(theHost, path);
        return r;
    }

    public static Resource findChild(Resource parent, Path path) throws NotAuthorizedException, BadRequestException {
        if (path.isRelative()) {
            return findChild(parent, path.getParts(), 0);
        } else {
            if( parent== null ) {   
                throw new IllegalArgumentException("Parent argument is null");
            }
            Host host = ((Templatable) parent).getHost();
            return findChild(host, path.getParts(), 0);
        }
    }

    public static Resource findChild(Resource parent, String childSpec) throws NotAuthorizedException, BadRequestException {
        switch (childSpec) {
            case ".":
                return parent;
            case "..":
                if (parent instanceof Templatable) {
                    Templatable ct = (Templatable) parent;
                    return ct.getParent();
                } else {
                    log.warn("Can't find parent of non CommonTemplated resource");
                    return null;
                }
            default:
                Resource child = null;
                if (parent instanceof CollectionResource) {
                    CollectionResource col = (CollectionResource) parent;
                    child = col.child(childSpec);
                    child = checkAndWrap(child, parent);
                }

                if (child == null && parent instanceof CommonTemplated) {
                    CommonTemplated t = (CommonTemplated) parent;
                    child = t.getChildResource(childSpec);
                    child = checkAndWrap(child, parent);
                }

                return child;
        }
    }

    public static Resource findChild(Resource parent, String[] arr, int i) throws NotAuthorizedException, BadRequestException {
        if (arr.length == 0) {
            return parent;
        }        

        String childName = arr[i];
        Resource child = findChild(parent, childName);        

        if (child == null) {
            LogUtils.trace(log, "findChild: parent=" , parent.getName(), "item=", arr[i] , "resource not found");
            return null;
        } else {
            if (i < arr.length - 1) {
                LogUtils.trace(log, "findChild: parent=" , parent.getName(), "item=", arr[i] , "resource=", child.getName(), " go to next level", i);
                return findChild(child, arr, i + 1);
            } else {
                LogUtils.trace(log, "findChild: parent=" , parent.getName(), "item=", arr[i] , "found resource=", child.getName());
                return child;
            }
        }
    }

    static Resource checkAndWrap(Resource r, Resource parent) {
        if (r == null) {
            return null;
        }

        LogUtils.trace(log, "checkAndWrap: ", r.getName());
        Resource r2;
        if (r instanceof SubPage) {
            log.trace("is a subpage");
            SubPage sub = (SubPage) r;
            if (sub.getParent() == parent) { // don't wrap if the request parent is same as physical parent
                log.trace("same parent, dont wrap");
                r2 = sub;
            } else {
                log.trace("not same parent, do wrap");
                r2 = new WrappedSubPage((SubPage) r, (CommonTemplated) parent);
            }
        } else if (r instanceof WrappedSubPage) {
            log.trace("wrap again");
            r2 = new WrappedSubPage((WrappedSubPage) r, (CommonTemplated) parent);
        } else {
            r2 = r;
        }
        return r2;
    }

    public static BaseResource get(UUID id) {
        VfsSession vfs = RequestContext.getCurrent().get(VfsSession.class);
        if (vfs == null) {
            throw new NullPointerException("No VFS session in context");
        }
        NameNode nn = vfs.get(id);
        if (nn == null) {
            return null;
        }
        return (BaseResource) nn.getData();
    }

    public static class HostNotFoundResource implements GetableResource {

        private final String hostName;

        public HostNotFoundResource(String hostName) {
            this.hostName = hostName;
        }

        private String getContent() {
            return "<html><body>The domain name " + hostName + " isnt on this server</body></html>";
        }

        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
            PrintWriter pw = new PrintWriter(out);
            pw.print(getContent());
            pw.flush();
            out.flush();
        }

        public Long getMaxAgeSeconds(Auth auth) {
            return null;
        }

        public String getContentType(String accepts) {
            return "text/html";
        }

        public Long getContentLength() {
            return (long) getContent().length();
        }

        public String getUniqueId() {
            return null;
        }

        public String getName() {
            return "";
        }

        public Object authenticate(String user, String password) {
            return null;
        }

        public boolean authorise(Request request, Method method, Auth auth) {
            return true;
        }

        public String getRealm() {
            return "NoRealm";
        }

        public Date getModifiedDate() {
            return null;
        }

        public String checkRedirect(Request request) {
            return null;
        }
    }
}
