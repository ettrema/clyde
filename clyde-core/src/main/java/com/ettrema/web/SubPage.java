package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.utils.Redirectable;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.InitUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

/**
 * Just a normal templated resource, except that the parent may not be a folder
 *
 * @author brad
 */
public class SubPage extends CommonTemplated implements Component, PostableResource, XmlPersistableResource, ISubPage, Redirectable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SubPage.class);
    private static final long serialVersionUID = 1L;
    String name;
    CommonTemplated parentPage;
    boolean secure;
    private boolean publicAccess;
    private String redirect;
    private boolean browsable;

    public SubPage(CommonTemplated parent, String name) {
        super();
        this.parentPage = parent;
        this.name = name;
    }

    public SubPage(Addressable container, Element el) {
        super();
        this.parentPage = (CommonTemplated) container;
        loadFromXml(el);
    }

    @Override
    public String getDefaultContentType() {
        return "text/html";
    }

    @Override
    public void sendContent(WrappedSubPage requestedPage, OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        requestedPage.generateContent(out, params);
    }

    /**
     * Not supported by default. Only implemented in sub classes
     *
     * @param requestedPage
     * @param in
     * @param length
     */
    @Override
    public void replaceContent(WrappedSubPage requestedPage, InputStream in, Long length) throws BadRequestException, NotAuthorizedException {
        log.error("Can't replace a SubPage content");
        throw new BadRequestException(this);
    }

    /**
     * if secure is set requires a logged in user
     *
     * @param request
     * @param method
     * @param auth
     * @return
     */
    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        if (secure) {
            if (auth == null) {
                return false;
            }
        }
        if (publicAccess) {
            return true;
        }
        return super.authorise(request, method, auth);
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public Folder getParentFolder() {
        if (parentPage instanceof Folder) {
            return (Folder) parentPage;
        }
        return parentPage.getParentFolder();
    }

    @Override
    public CommonTemplated getParent() {
        return parentPage;
    }

    /**
     * For backwards compatibility, same as getParent
     *
     * @return
     */
    public CommonTemplated getFoundParent() {
        return getParent();
    }

    @Override
    public void loadFromXml(Element el) {
        this.name = el.getAttributeValue("name");
        this.secure = InitUtils.getBoolean(el, "secure");
        if (name == null || name.trim().length() == 0) {
            throw new RuntimeException("name is blank");
        }
        this.publicAccess = InitUtils.getBoolean(el, "publicAccess");
        this.browsable = InitUtils.getBoolean(el, "browsable");
        this.redirect = InitUtils.getValue(el, "redirect");
        super.loadFromXml(el);

    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = super.toXml(container, el);
        InitUtils.setString(e2, "name", name);
        InitUtils.setBoolean(e2, "secure", secure);
        InitUtils.setBoolean(e2, "publicAccess", publicAccess);
        InitUtils.setBoolean(e2, "browsable", browsable);
        InitUtils.setString(e2, "redirect", redirect);
        return e2;
    }

    @Override
    public boolean validate(RenderContext rc) {
        return true;
    }

    public final void setValidationMessage(String s) {
        RequestParams params = RequestParams.current();
        params.attributes.put(this.getName() + "_validation", s);
    }

    @Override
    public final String getValidationMessage() {
        RequestParams params = RequestParams.current();
        return (String) params.attributes.get(this.getName() + "_validation");
    }

    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
        ITemplate lTemplate = getTemplate();
        RenderContext rcChild = new RenderContext(lTemplate, this, rc, false);
        String redirectTo = null;
        for (Component c : allComponents()) {
            redirectTo = c.onProcess(rcChild, parameters, files);
            if (redirectTo != null) {
                return redirectTo;
            }
        }
        return null;
    }

    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        ITemplate lTemplate = getTemplate();
        RenderContext rcChild = new RenderContext(lTemplate, this, rc, false);
        for (Component c : this.getComponents().values()) {
            c.onPreProcess(rcChild, parameters, files);
        }

        Collection<Component> all = allComponents();
        for (Component c : all) {
            c.onPreProcess(rcChild, parameters, files);
        }
    }

    @Override
    public Path getPath() {
        Path p = parentPage.getPath().child(name);
        return p;
    }

    @Override
    public String getHref() {
        String s = parentPage.getHref();
        if (!s.endsWith("/")) {
            s = s + "/";
        }
        s = s + name;
        return s;
    }

    @Override
    public String getRealm() {
        return parentPage.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) {
        return null;
    }

    @Override
    public Web getWeb() {
        return Web.find(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void init(Addressable container) {
        this.parentPage = (BaseResource) container;
    }

    @Override
    public Addressable getContainer() {
        return this.parentPage;
    }

    void setRequestParent(Resource parent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void loadFromXml(Element el, Map<String, String> params) {
        this.loadFromXml(el);
    }

    @Override
    public Element toXml(Element el, Map<String, String> params) {
        return this.toXml(null, el);
    }

    @Override
    public void save() {
        getPhysicalParent().save();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private BaseResource getPhysicalParent() {
        return physicalParent(this.parentPage);
    }

    private BaseResource physicalParent(Templatable parentPage) {
        if (parentPage instanceof BaseResource) {
            return (BaseResource) parentPage;
        }
        return physicalParent(parentPage.getParent());
    }

    @Override
    public Date getCreateDate() {
        return getPhysicalParent().getCreateDate();
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    @Override
    public boolean isPublicAccess() {
        return publicAccess;
    }

    @Override
    public String getRedirect() {
        return this.redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public boolean isBrowsable() {
        return browsable;
    }

    public void setBrowsable(boolean browsable) {
        this.browsable = browsable;
    }

    @Override
    public List<WebResource> getWebResources() {
        return null;
    }
    
    
}
