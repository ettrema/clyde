
package com.ettrema.web;

import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.DeleteCommand;
import com.ettrema.web.component.HtmlDef;
import com.ettrema.web.component.HtmlInput;
import com.ettrema.web.component.SaveCommand;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.web.security.Subject;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class Root extends CommonTemplated implements ITemplate {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Root.class);
    public static final String HTML_TEMPLATE = "$rc.docTypeDec\n$rc.doBody()";
    
    private static final long serialVersionUID = 1L;
    
    //private static final Map<Folder,Root> cache = new HashMap<Folder,Root>();
    //private static final Map<Folder,Root> cache = new WeakHashMap<Folder, Root>();
    
    public static synchronized Root getInstance(Folder templates) {
        return new Root("root",templates);
//        Root r = cache.get(templates);
//        if( r == null ) {
//            r = new Root("root",templates);
//            cache.put(templates, r);
//        }
//        return r;
    }

    private final String name;

    private final ComponentDefMap componentDefs = new ComponentDefMap();

    private final Folder templates;

    
    private Root(String name, Folder templates) {
        this.name = name;
        this.templates = templates;

              
        HtmlInput root = new HtmlInput(this, "root");
        root.cols = 80;
        root.rows = 30;
        root.setValue(HTML_TEMPLATE);
        this.getComponents().add(root);

        Component c;
        c = new SaveCommand(this, "save");
        this.getComponents().add(c);
        c = new DeleteCommand(this, "delete");
        this.getComponents().add(c);
        
        HtmlDef body = new HtmlDef(this, "body");
        this.getComponentDefs().add(body);
    }


    @Override
    public Folder createFolderFromTemplate(Folder location, String name) {
        return new Folder(location, name);
    }

    @Override
    public BaseResource createPageFromTemplate(Folder location, String name, InputStream in, Long length) {
        BaseResource res = createPageFromTemplate(location, name);
        res.save();
        res.setContent(in);
        return res;
    }

    @Override
    public BaseResource createPageFromTemplate(Folder location, String name) {
        return new Page(location, name);
    }

    @Override
    public ComponentDefMap getComponentDefs() {
        return componentDefs;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommonTemplated getParent() {
        return templates;
    }

    @Override
    public String getDefaultContentType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getRealm() {
        return templates.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return null;
    }

    @Override
    public ComponentDef getComponentDef(String name) {
        return componentDefs.get(name);
    }

    @Override
    public boolean represents(String type) {
        return false;
    }

    @Override
    public boolean canCreateFolder() {
        return true;
    }

    @Override
    public void onBeforeSave(BaseResource aThis) {
   
    }

    

    @Override
    public void onAfterSave(BaseResource aThis) {

    }

    @Override
    public DocType getDocType() {
        return null;
    }

    @Override
    public Boolean isSecure() {
        return null;
    }

    @Override
    public Boolean hasRole(Subject user, Role role, CommonTemplated target) {
        return null;
    }

    @Override
    public String onPost(CommonTemplated aThis) {
        return null;
    }

    @Override
    public Boolean isEnableGetableFolders() {
        return false;
    }

    @Override
    public List<WebResource> getWebResources() {
        return null;
    }

    
}
