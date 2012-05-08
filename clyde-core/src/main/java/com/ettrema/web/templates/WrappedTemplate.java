package com.ettrema.web.templates;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.CommonTemplated.Params;
import com.ettrema.web.*;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.web.security.Subject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class WrappedTemplate implements ITemplate{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WrappedTemplate.class);
    
    private final ITemplate template;
    private final Web web;

    public WrappedTemplate(ITemplate template, Web web) {
        this.template = template;
        this.web = web;
    }
    
    
    
    @Override
    public Folder createFolderFromTemplate(Folder location, String name) {
        return template.createFolderFromTemplate(location, name);
    }

    @Override
    public BaseResource createPageFromTemplate(Folder location, String name, InputStream in, Long length) {
        return template.createPageFromTemplate(location, name, in, length);
    }

    @Override
    public BaseResource createPageFromTemplate(Folder location, String name) {
        return template.createPageFromTemplate(location, name);
    }

    @Override
    public Component _invoke(String name) {
        return template._invoke(name);
    }

    @Override
    public String render(RenderContext child, Map<String, String> params, ITemplate t) {
        return template.render(child, params, t);
    }

    @Override
    public ComponentDef getComponentDef(String name) {
        return template.getComponentDef(name);
    }

    @Override
    public ComponentDefMap getComponentDefs() {
        return template.getComponentDefs();
    }

    @Override
    public boolean represents(String type) {
        return template.represents(type);
    }

    @Override
    public boolean canCreateFolder() {
        return template.canCreateFolder();
    }

    @Override
    public String onPost(CommonTemplated aThis) {
        return template.onPost(aThis);
    }

    @Override
    public void onBeforeSave(BaseResource aThis) {
        template.onBeforeSave(aThis);
    }

    @Override
    public void onAfterSave(BaseResource aThis) {
        template.onAfterSave(aThis);
    }

    @Override
    public DocType getDocType() {
        return template.getDocType();
    }

    @Override
    public Boolean isSecure() {
        return template.isSecure();
    }

    @Override
    public Boolean hasRole(Subject user, Role role, CommonTemplated target) {
        return template.hasRole(user, role, target);
    }

    @Override
    public Boolean isEnableGetableFolders() {
        return template.isEnableGetableFolders();
    }

    @Override
    public List<WebResource> getWebResources() {
        return template.getWebResources();
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        template.sendContent(out, range, params, contentType);
    }

    @Override
    public String getTemplateName() {
        return template.getTemplateName();
    }

    @Override
    public ITemplate getTemplate() {
        ITemplate template = null;
        Web web = getWeb();
        if (web != null) {
            String templateName = getTemplateName();
            if (templateName == null || templateName.length() == 0 || templateName.equals("null")) {
                LogUtils.trace(log, "getTemplate: empty template name for", getName());
                return null;
            }
            TemplateManager tm = _(TemplateManager.class);
            template = tm.lookup(templateName, web);
            if (template == null) {
                LogUtils.trace(log, "getTemplate: no template", templateName, "for web=", web.getName());
            } else {
                if (template == this) {
                    throw new RuntimeException("my template is myself");
                }
            }
        } else {
            LogUtils.trace(log, "getTemplate: no web for", this.getName());
        }
        return template;        
    }

    @Override
    public Collection<Component> allComponents() {
        return template.allComponents();
    }

    @Override
    public Component getComponent(String paramName, boolean includeValues) {
        return template.getComponent(paramName, includeValues);
    }

    @Override
    public boolean is(String type) {
        return template.is(type);
    }

    @Override
    public ComponentValueMap getValues() {
        return template.getValues();
    }

    @Override
    public ComponentMap getComponents() {
        return template.getComponents();
    }

    @Override
    public void preProcess(RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files) {
        template.preProcess(rcChild, parameters, files);
    }

    @Override
    public String process(RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
        return template.process(rcChild, parameters, files);
    }

    @Override
    public String getHref() {
        return web.getTemplates().getHref() + getName();
    }

    @Override
    public String getUrl() {
        return web.getTemplates().getUrl() + getName();
    }

    @Override
    public Web getWeb() {
        return web;
    }

    @Override
    public Host getHost() {
        return web.getHost();
    }

    @Override
    public Folder getParentFolder() {
        return web.getTemplates();
    }

    @Override
    public Templatable find(Path path) {
        return web.getTemplates().find(path);
    }

    @Override
    public Templatable getParent() {
        return getParentFolder();
    }

    @Override
    public Params getParams() {
        return template.getParams();
    }

    @Override
    public String getContentType(String accepts) {
        return template.getContentType(accepts);
    }

    @Override
    public Date getCreateDate() {
        return template.getCreateDate();
    }

    @Override
    public String getUniqueId() {
        return template.getUniqueId() + web.getUniqueId();
    }

    @Override
    public String getName() {
        return template.getName();
    }

    @Override
    public Object authenticate(String string, String string1) {
        return web.authenticate(string, string1);
    }

    @Override
    public boolean authorise(Request rqst, Method method, Auth auth) {
        return web.authorise(rqst, method, auth);
    }

    @Override
    public String getRealm() {
        return web.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return template.getModifiedDate();
    }

    @Override
    public String checkRedirect(Request rqst) {
        return template.checkRedirect(rqst);
    }

    @Override
    public Addressable getContainer() {
        return web.getTemplates();
    }

    @Override
    public Path getPath() {
        return web.getTemplates().getPath().child(getName());
    }

    @Override
    public int compareTo(Resource o) {
        return template.compareTo(o);
    }

    @Override
    public Component getAnyComponent(String name) {
        return template.getAnyComponent(name);
    }

}
