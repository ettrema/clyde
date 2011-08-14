package com.bradmcevoy.web;

import com.bradmcevoy.web.SimpleEditPage.SimpleEditable;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.web.eval.EvalUtils;
import com.bradmcevoy.web.eval.Evaluatable;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.utils.GroovyUtils;
import com.bradmcevoy.utils.LogUtils;
import com.bradmcevoy.utils.ReflectionUtils;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.EmailDef;
import com.bradmcevoy.web.component.HtmlDef;
import com.bradmcevoy.web.component.NumberDef;
import com.bradmcevoy.web.component.Text;
import com.bradmcevoy.web.component.TextDef;
import com.bradmcevoy.web.security.CurrentUserService;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import com.bradmcevoy.web.security.Subject;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;


import static com.ettrema.context.RequestContext._;

public class Template extends Page implements ITemplate, SimpleEditable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Template.class);
    private static final long serialVersionUID = 1L;
    private DocType docType;
    private final ComponentDefMap componentDefs = new ComponentDefMap();
//    private transient Component addParam;
    private String afterCreateScript;
    private String beforeSaveScript;
    private String afterSaveScript;
    /**
     * Script to call when a page of this template is POST'ed to.
     */
    private String onPostPageScript;
    private Boolean secure; // if true, will make instances of this template secure. May be overridden
    
    /**
     * true indicates that content items of this type will not be exported
     */
    private boolean disableExport;
    

    public Template(Folder parent, String name) {
        super(parent, name);
    }

    @Override
    protected BaseResource copyInstance(Folder parent, String newName) {
        Template newRes = (Template) super.copyInstance(parent, newName);
        newRes.componentDefs.addAll(this.componentDefs);
        return newRes;
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        componentDefs.init(this);
    }

    @Override
    public void loadFromXml(Element el) {
        log.trace("loadFromXml");
        super.loadFromXml(el);
        componentDefs.fromXml(this, el);
        Element elScript = el.getChild("afterCreateScript");
        if (elScript != null) {
            afterCreateScript = elScript.getText();
        }
        elScript = el.getChild("afterSaveScript");
        log.trace("loadFromXml: " + el.getName() + elScript);
        if (elScript != null) {
            afterSaveScript = elScript.getText();
            log.trace("loadFromXml: afterSaveScript: " + afterSaveScript);
        }
        
        elScript = el.getChild("onPostPageScript");
        log.trace("loadFromXml: " + el.getName() + elScript);
        if (elScript != null) {
            onPostPageScript = elScript.getText();
            log.trace("loadFromXml: onPostPageScript: " + afterSaveScript);
        }        
        
        String dt = InitUtils.getValue(el, "docType");
        docType = dt == null ? null : DocType.valueOf(dt);

        secure = InitUtils.getNullableBoolean(el, "secure");
        
    }

    @Override
    public void populateXml(Element e2) {
        log.trace("populateXml");
        super.populateXml(e2);
        componentDefs.toXml(this, e2);
        if (afterCreateScript != null) {
            Element elScript = new Element("afterCreateScript");
            elScript.setText(afterCreateScript);
            e2.addContent(elScript);
        }
        if (afterSaveScript != null) {
            Element elScript = new Element("afterSaveScript");
            elScript.setText(afterSaveScript);
            e2.addContent(elScript);
            log.trace("populateXml: afterSaveScript: " + afterSaveScript);
        }
        if (onPostPageScript != null) {
            Element elScript = new Element("onPostPageScript");
            elScript.setText(onPostPageScript);
            e2.addContent(elScript);
            log.trace("populateXml: onPostPageScript: " + onPostPageScript);
        }        
        String dt = getDocType() == null ? null : getDocType().name();
        InitUtils.set(e2, "docType", dt);

        InitUtils.set(e2, "secure", secure);        
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        return super.toXml(container, el);
    }

    @Override
    public boolean is(String type) {
        if (type.equalsIgnoreCase("template")) {
            return true;
        }
        return super.is(type);
    }

    @Override
    public boolean represents(String type) {
        String tname = getName();
        boolean b = type.equals(tname) || (type + ".html").equals(tname);
        if (b) {
			LogUtils.trace(log, "represents: type matches template name");
            return true;
        }
		if( "folder".equals(type)) {
			return canCreateFolder();
		}
        ITemplate parent = getTemplate();
        if (parent != null) {
            return parent.represents(type);
        } else {
            return false;
        }

    }

    @Override
    public Component getAnyComponent(String childName) {
        // not, for rendercontext.invoke to work properly, this must return cdef's in preference to cvalues
        Component c = getComponentDef(childName);
        if (c != null) {
            return c;
        }

        return super.getAnyComponent(childName);
    }

    @Override
    public ComponentDef getComponentDef(String name) {
        ComponentDef def = getComponentDefs().get(name);
        if (def != null) {
            return def;
        }
        ITemplate t = this.getTemplate();
        if (t == null) {
            return null;
        }
        return t.getComponentDef(name);
    }

    @Override
    public BaseResource createPageFromTemplate(Folder location, String name, InputStream in, Long length) {
        BaseResource res = createPageFromTemplate(location, name);
        res.save();
        res.setContent(in);
        return res;
    }

    /**
     * 
     * @param location
     * @param name
     * @return - a newly created, but not saved, baseresource
     */
    @Override
    public BaseResource createPageFromTemplate(Folder location, String name) {
        log.debug("createPageFromTemplate");
        BaseResource newRes;
        if (location.getName().equals(this.getParent().getName())) {
//            log.debug("  creating a template, because in templates folder"); // hack alert
            newRes = new Template(location, name);
        } else {
            newRes = newInstanceFromTemplate(location, name);
            if (newRes == null) {
                log.debug("  creating a page because nothing else specified");
                newRes = new Page(location, name);
            } else {
                log.debug("  created a: " + newRes.getClass());
            }
        }
        IUser creator = _(CurrentUserService.class).getOnBehalfOf();
        if (creator instanceof User) {
            newRes.setCreator((User) creator);
        }

        newRes.setTemplate(this);
        for (ComponentDef def : componentDefs.values()) {
            ComponentValue cv = def.createComponentValue(newRes);
            newRes.getValues().add(cv);
        }
        execAfterScript(newRes);
        return newRes;
    }

    @Override
    public Folder createFolderFromTemplate(Folder location, String name) {
        log.debug("createFolderFromTemplate");
        Folder newRes;
        newRes = (Folder) newInstanceFromTemplate(location, name);
        if (newRes == null) {
            newRes = new Folder(location, name);
        }

        newRes.setTemplate(this);

        for (ComponentDef def : componentDefs.values()) {
            ComponentValue cv = def.createComponentValue(newRes);
            log.debug("createFolderFromTemplate: created a: " + cv.getClass() + " def:" + def.getName());
            newRes.getValues().add(cv);
        }
        execAfterScript(newRes);
        return newRes;
    }

    @Override
    public Collection<Component> allComponents() {
        Collection<Component> set = super.allComponents();
        set.addAll(componentDefs.values());
//        set.add( getAddParameterComponent() );
        return set;
    }

    private BaseResource newInstanceFromTemplate(Folder location, String name) {
        if (location == null) {
            throw new IllegalArgumentException("location is null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        String sClass = getClassToCreate();
        if (StringUtils.isEmpty(sClass)) {
            log.trace("no class to create specified, so default to page");
            Page p = new Page(location, name);
            p.setTemplateName(this.getName());
            return p;
        } else {
            if (log.isTraceEnabled()) {
                log.trace("creating a '" + sClass + "' called: " + name);
            }
            Class clazz = ReflectionUtils.findClass(sClass);
            BaseResource res = (BaseResource) ReflectionUtils.create(clazz, location, name);
            res.setTemplateName(this.getName());
            return res;
        }
    }

    @Override
    public boolean canCreateFolder() {
        String s = getClassToCreate();
        if (StringUtils.isEmpty(s)) {
            return false;
        } else {
            return Folder.class.getCanonicalName().equals(s);
        }
    }

    public String getClassToCreate() {
        Component c = this.getComponent("class");
        String sClass = null;
        if (c != null) {
            Text t = (Text) c;
            sClass = t.getValue();
        }
        if (sClass == null) {
            return null;
        } else {
            return sClass.trim();
        }
    }

    public void setClassToCreate(String s) {
        Text c = (Text) this.getComponent("class");
        if (c == null) {
            c = new Text(this, "class");
            this.getComponents().add(c);
        }
        c.setValue(s);
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new Template(parent, newName);
    }

    @Override
    public ComponentDefMap getComponentDefs() {
        return componentDefs;
    }

    private void execAfterScript(BaseResource newlyCreated) {
        if (afterCreateScript == null) {
            return;
        }
        log.debug("execAfterScript");
        Map map = new HashMap();
        map.put("created", newlyCreated);
        map.put("command", this);
        Templatable ct = (Templatable) this.getContainer();
        GroovyUtils.exec(ct, map, afterCreateScript);
        log.debug("done execAfterScript");
    }

    @Override
    public void onBeforeSave(BaseResource aThis) {
        if (beforeSaveScript != null) {
            if (!this.isTrash()) { // this means it has been soft-deleted. Should be handled by afterDelete
                log.trace("onAfterSave: run script");
                Map map = new HashMap();
                GroovyUtils.exec(aThis, map, beforeSaveScript);
            }
        }
    }



    @Override
    public void onAfterSave(BaseResource aThis) {
        if (afterSaveScript != null) {
            if (!this.isTrash()) { // this means it has been soft-deleted. Should be handled by afterDelete
                log.trace("onAfterSave: run script");
                Map map = new HashMap();
                GroovyUtils.exec(aThis, map, afterSaveScript);
            }
        }
    }

    @Override
    public String onPost(CommonTemplated aThis) {
        if( onPostPageScript != null ) {
            if (!this.isTrash()) { // this means it has been soft-deleted. Should be handled by afterDelete
                log.trace("onPost: run script");
                Map map = new HashMap();
                Object result = GroovyUtils.exec(aThis, map, onPostPageScript);
                if( result instanceof String ) {
                    return result.toString();
                }
            }            
        }
        ITemplate t = getTemplate();
        if( t != null ) {
            return t.onPost(aThis);
        }
        return null;
    }

    

    public TextDef addTextDef(String name) {
        TextDef d = new TextDef(this, name);
        this.componentDefs.add(d);
        return d;
    }

    public NumberDef addNumberDef(String name) {
        NumberDef d = new NumberDef(this, name);
        this.componentDefs.add(d);
        return d;
    }

    public HtmlDef addHtmlDef(String name) {
        HtmlDef d = new HtmlDef(this, name);
        this.componentDefs.add(d);
        return d;
    }

    public EmailDef addEmailDef(String name) {
        EmailDef d = new EmailDef(this, name);
        this.componentDefs.add(d);
        return d;
    }

    public String getAfterCreateScript() {
        return afterCreateScript;
    }

    public void setAfterCreateScript(String afterCreateScript) {
        this.afterCreateScript = afterCreateScript;
    }

    public String getBeforeSaveScript() {
        return beforeSaveScript;
    }

    public void setBeforeSaveScript(String beforeSaveScript) {
        this.beforeSaveScript = beforeSaveScript;
    }


    public String getAfterSaveScript() {
        return afterSaveScript;
    }

    public void setAfterSaveScript(String afterSaveScript) {
        this.afterSaveScript = afterSaveScript;
    }

    @Override
    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public boolean isDisableExport() {
        return disableExport;
    }

    public void setDisableExport(boolean disableExport) {
        this.disableExport = disableExport;
    }
    
    @Override
    public Boolean isSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    @Override
    public Boolean hasRole(Subject user, Role role, CommonTemplated target) {
        Evaluatable rules = getRoleRules();
        if (rules != null ) {
            log.trace("hasRole - found rules");
            RenderContext rc = new RenderContext(this, target, null, false);
            Object r = EvalUtils.eval(rules, rc, target);
            Boolean result = Formatter.getInstance().toBool(r);
            return result;
        } else {
            log.trace("hasRole - no rules defined");
            return null;
        }
    }

    public String getOnPostPageScript() {
        return onPostPageScript;
    }

    public void setOnPostPageScript(String onPostPageScript) {
        this.onPostPageScript = onPostPageScript;
    }

    @Override
    public PostableResource getEditPage() {
        System.out.println("get simple edit page ---");
        return new SimpleEditPage( this );
    }

    @Override
    public void setContent(String content) {
        System.out.println("setContent: " + content);
        ComponentValue cvBody = this.getValues().get("body");
        if( cvBody == null ) {
            System.out.println("add cv");
            cvBody = new ComponentValue("body", this);
            this.getValues().add(cvBody);
        }
        cvBody.setValue(content);
        this.save();
    }

    @Override
    public String getContent() {
        ComponentValue cvBody = this.getValues().get("body");
        if( cvBody == null ) {
            return "";
        } else {
            return cvBody.getFormattedValue(this);
        }
    }
    
    
    
}
