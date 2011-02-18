package com.bradmcevoy.web;

import java.util.Iterator;
import java.util.ArrayList;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.utils.CurrentRequestService;
import com.bradmcevoy.web.component.Command;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentUtils;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.DeleteCommand;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import com.ettrema.context.RequestContext;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;

import static com.ettrema.context.RequestContext._;

/**
 * What is a RenderContext?
 * When a page is rendered it first calls its template
 * to render it. The template generates the layout and inserts the page's content
 * into that layout.
 *
 * This means that a template *delegates* to the page it is rendering. However,
 * a template doesnt normally have a reference to the page, because it is the page
 * which has a reference to the template.
 *
 * To get around this problem we have a RenderContext (or RC). A RC is just a
 * reference to a page or template, and another RC - called the child - and an edit mode. When
 * rendering a template and an "invoke" instruction is encountered, the RC will
 * find an appropriate component definition, and then use that to render the
 * corresponding component value from the child RC. This is because it is definitions
 * which know how to render values, not the value itself.
 *
 * Edit Mode
 * Components can be rendered in view mode or edit mode. Typically, a page is put
 * into edit mode by accessing it on some special URL pattern and this causes all
 * components to display in edit mode, allowing the content to be edited.
 *
 * However, there is a subtle issue about the relationship between RC's, pages and
 * the edit mode. Assume we are rendering page A which has a template B. There
 * will be an RC for each, but only the RC for page A will have its edit mode set.
 * Thats because we don't want to edit the layout in template B, we want to edit
 * the content in page A.
 *
 * But, as discussed above, it is the component definition which renders values,
 * not the value itself. So it is the RC for template B which must make the decision
 * to render in edit mode or view mode, even though the edit mode flag is set to true
 * on the child RC.
 *
 * However, values don't always have to come from the immediate child, they
 * can come from subsequent children, and in this case the edit mode to be applied
 * is that on the RC which contains the value.
 *
 * So rule 1:
 * Edit mode to render with for a component definition is the edit mode of the
 * RC which holds the value.
 *
 * @author brad
 */
public class RenderContext implements Map<String, Component> {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RenderContext.class);
    private static final ReplaceableHtmlParser PARSER = new ReplaceableHtmlParserImpl();
    final public ITemplate template;
    final public Templatable page;
    final public RenderContext child;
    final public boolean pageEditMode;
    final Map<String, Object> attributes = new HashMap<String, Object>();

    public RenderContext(ITemplate template, Templatable page, RenderContext child, boolean editMode) {
        if( page == null ) {
            throw new IllegalArgumentException("page cannot be null");
        }
        this.template = template;
        this.page = page;
        this.child = child;
        this.pageEditMode = editMode;
    }

    public boolean hasRole(String s) {
        PermissionChecker permissionChecker = RequestContext.getCurrent().get(PermissionChecker.class);
        Role r = Role.valueOf(s);
        return permissionChecker.hasRole(r, getTargetPage(), RequestParams.current().getAuth());
    }

    public ITemplate getTemplate() {
        return template;
    }

    public String getDocTypeDec() {
        if( child == null || child.page == null ) {
            return "";
        }
        ITemplate.DocType dt;
        if( child.page instanceof ITemplate ) {
            ITemplate t = (ITemplate) child.page;
            dt = t.getDocType();
        } else {
            return "";
        }

        if( dt == null ) {
            return "";
        }
        switch( dt ) {
            case STRICT:
                return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
            case TRANSITIIONAL:
                return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
            case XSTRICT:
                return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
            case XTRANSITIONAL:
                return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
            default:
                throw new RuntimeException("Unsupported doc type");                        
        }

    }

    public String getTemplateHost() {
        return template.getHost().getName();
    }

    public DateTime toJodaDate(Date dt) {
        return new DateTime(dt.getTime());
    }

    public void addAttribute(String key, Object val) {
        attributes.put(key, val);
    }

    /**
     * gets an attribute by key
     * 
     * @param key
     * @return
     */
    public Object getAttribute(String key) {
        Object o = attributes.get(key);
        if (o == null) {
            log.warn("not found: " + key + " size:" + attributes.size());
        }
        return o;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getFormStart() {
        RequestParams params = RequestParams.current();
        String url;
        if (params != null) {
            url = params.href;
        } else {
            url = "";
        }
        String s = "<form method=\"post\" action=\"" + url + "\">";
        if (params != null && params.parameters != null && params.parameters.containsKey(NewPage.selectName())) { // oh my god, this is horrible
            s = s + "<input type='hidden' name='" + NewPage.selectName() + "' value='" + params.parameters.get(NewPage.selectName()) + "' />";
        }
        return s;
    }

    public String getFormEnd() {
        return "</form>";
    }

    public String templateResource(String name) {
        Templatable target = this.getTargetPage();
        if (target instanceof Template) {
            return name;
        } else {
            Folder templates = target.getWeb().getTemplates();
            BaseResource r = templates.childRes(name);
            if (r == null) {
                log.warn("Did not find template resource: " + name + " in folder: " + templates.getPath());
                return templates.getHref() + name;
            } else {
                return r.getHref();
            }
        }
    }

    public Auth getAuth() {
        return RequestParams.current().getAuth();
    }

    public String getActualHref() {
        return RequestParams.current().href;
    }

    public RenderContext getChild() {
        return child;
    }

    public boolean getEditMode() {
        return pageEditMode;
    }

    public Templatable getMe() {
        return page;
    }

    public RenderContext getTarget() {
        if (child == null) {
            return this;
        } else {
            return child.getTarget();
        }
    }

    /**
     * Return the actual page identified in the request. This is the page
     * associated with teh target request context
     *
     * @return
     */
    public Templatable getTargetPage() {
        if (child == null) {
            return page;
        } else {
            return child.getTargetPage();
        }
    }

    public RenderContext getTargetContext() {
        if (child == null) {
            return this;
        } else {
            return child.getTargetContext();
        }
    }

    public String doBody() {
        if (child == null) {
            if (log.isDebugEnabled()) {
                log.debug("dobody: no child context so cant delegate, returning empty");
            }
            return "";
        }
        String s = doBody(child);
        if (s == null) {
            s = "";
        }
        return s;
    }

    /** Returns the rendered body component value for this page
     */
    public String doBody(RenderContext rcChild) {
        //log.debug( "doBody: page: " + rcChild.page.getName());
        Templatable childPage = rcChild.page;
        ComponentValue cvBody = childPage.getValues().get("body");
        if (cvBody == null) {
            cvBody = new ComponentValue("body", childPage);
            cvBody.init(childPage);
            childPage.getValues().add(cvBody);
        }
        if (rcChild.pageEditMode) {
            //log.debug( "edit");
            return cvBody.renderEdit(rcChild);
        } else {
            //log.debug( "not edit: isTemplate" + (rcChild.page instanceof Template) + " - child is null: " + (rcChild.child == null));
            if (rcChild.child == null && rcChild.page instanceof Template) {
                log.debug("output source");
                Object val = cvBody.getValue();
                if (val == null) {
                    return "";
                } else {
                    return wrapWithIdentifier(val.toString(), "body");
                }
            } else {
                String body = cvBody.render(rcChild);
                if (rcChild.child == null) {
                    return wrapWithIdentifier(body, "body");
                } else {
                    return body;
                }
            }
        }
    }

    public String invoke(String paramName) {
        return invoke(paramName, null, true);
    }

    public String invoke(String paramName, Boolean editable) {
        return invoke(paramName, editable, editable);
    }

    private String invoke(String paramName, Boolean componentEdit, Boolean markers) {
        log.debug("invoke: " + paramName + " on " + this.page.getName());
        try {
            Path p = Path.path(paramName);
            // First, look for a component in this page
            Component c = ComponentUtils.findComponent(p, page);
            if (c == null) {
                log.debug("component not found: " + p + " in: " + page.getHref());
                return "";
            } else {
                log.debug("found component: " + c.getClass() + " - " + c.getName() + " from path: " + p);
                String s;
                if (c instanceof ComponentDef) {
                    log.trace("found componentdef");
                    ComponentDef def = (ComponentDef) c;
                    return renderDef(componentEdit, def, markers);
                } else {
                    log.debug("not a componentdef: " + c.getClass());
                    RenderContext childRc = this.child == null ? this : this.child;
                    if (editMode(componentEdit)) {
                        s = c.renderEdit(childRc);
                    } else {
                        s = c.render(childRc);
                    }
                    if (s == null) {
                        s = "";
                    }
                    return s;
                }
            }
        } catch (Exception e) {
            log.error("exception invoking: " + paramName, e);
            return "ERR: " + paramName + " : " + e.getMessage();
        }
    }

    /**
     *         All sorts of crazy going on with editable and component value location
    So we want user to define the user fields, but we then want pharmacist and pa
    to inherit those definitions, and we want values on the pages
    So when we call rc.invoke('firstName') that won't find a value on pharmacist
    but it should look for one on the page. It should then be displayed as editable
     *
     * @param editable
     * @param def
     * @param markers
     * @return
     */
    private String renderDef(Boolean editable, ComponentDef def, Boolean markers) {
        RenderContext childRc = this.child == null ? this : this.child;
        Templatable nextPage = null;
        if (this.child != null) {
            nextPage = this.child.page;
        }
        ComponentValue cv;
        if (this.child != null) {
            cv = getComponentValue(def.getName(), this.child.page);
        } else {
            cv = null;
        }
        if (cv == null && editMode(editable) && nextPage instanceof BaseResource) {
            cv = def.createComponentValue((BaseResource) nextPage);
            nextPage.getValues().add(cv);
        }
        if (cv == null) {
            log.trace("look for child value");
            if (this.child != null) {
                return this.child.renderDef(editable, def, markers);
            } else {
                log.trace("no value found");
                return "";
            }
        } else {
            log.debug("rendering cv:" + pageEditMode + " - " + editable);
            String s;
            if (editMode(editable)) {
                s = cv.renderEdit(childRc);
            } else {
                s = cv.render(childRc);
            }
            if (log.isTraceEnabled()) {
                log.trace(" - result:" + s);
            }
            if (s == null) {
                s = "";
            }
            log.debug("!editmod " + !pageEditMode + " markers:" + markers);
            if (!pageEditMode && markers != null && markers) {
                return wrapWithIdentifier(s, def.getName());
            } else {
                return s;
            }
        }
    }

    /**
     * If a not-null value has been given for the component, then that defines
     * the edit mode of the rendering of the component.
     *
     * But if none is given then fallback on the page edit mode
     * 
     * @param componentEditable
     * @return
     */
    private boolean editMode(Boolean componentEditable) {
        log.trace("editMode: " + this.page.getName() + "  page: " + pageEditMode + " component: " + componentEditable);

        if (componentEditable != null) {
            return componentEditable;
        } else {
            if (this.child == null) {
                return false;
            } else {
                return this.child.pageEditMode;
                //return this.pageEditMode;
            }
        }
    }

    public ComponentValue getComponentValue(String name, Templatable page) {
        if (page == null) {
            log.trace("no cv found, return null");
            return null;
        }
        ComponentValue cv = page.getValues().get(name);
        if (cv != null) {
            if (cv.getContainer() == null) {
                log.trace("no container, so init");
                cv.init(page);
            }
            return cv;
        }
        return cv;
    }

    public String invoke(Templatable page, String paramName) {
        RenderContext rc = new RenderContext(page.getTemplate(), page, null, false);
        return rc.invoke(paramName);
    }

    public String invokeForEdit(String paramName) {
        if (child != null && child.pageEditMode) {
            return invoke(paramName, true);
        } else {
            return "";
        }
    }

    public String invoke(Component c, boolean editable) {
        String s = c.renderEdit(child);
        if (s == null) {
            s = "";
        }
        return s;

    }

    public String invoke(Component c) {
        return c.render(child);
    }

    public String invokeEdit(String paramName) {
        Component c = this.getTargetPage().getComponent(paramName, false);
        if (c != null) {
            return c.renderEdit(child);
        }
        ComponentValue cv;
        if (child != null && child.getMe() != null) {
            cv = child.getMe().getValues().get(paramName);
        } else {
            cv = new ComponentValue(paramName, null);
        }
        if (cv == null) {
            log.error("no parameter " + paramName + " in param values from " + child.getMe().getName());
            return null;
        }
        return cv.renderEdit(child);
    }

    /** Return html for the child's body
     */
    public String getToolBar() {
        StringBuilder sb = new StringBuilder();
        Templatable targetPage = getTargetPage();
        Collection<Component> list = ComponentUtils.allComponents(targetPage);
        for (Component c : list) {
            if (c instanceof Command) {
                Command cmd = (Command) c;
                if (cmd instanceof DeleteCommand) {
                    if (!isNew(child)) {
                        sb.append(cmd.render(child));
                    }
                } else {
                    sb.append(cmd.render(child));
                }

            }
        }
        return sb.toString();
    }

    private boolean isNew(RenderContext child) {
        if( child == null ) {
            return false;
        }
        Templatable target = child.getTargetPage();
        if (target == null) {
            return true;
        } else {
            if (target instanceof BaseResource) {
                BaseResource res = (BaseResource) target;
                boolean b = res.isNew();
                return b;
            } else {
                return false;
            }
        }
    }

    public boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        } else if (o instanceof String) {
            String s = (String) o;
            return s.trim().length() == 0;
        } else if (o instanceof Collection) {
            Collection col = (Collection) o;
            return col.isEmpty();
        } else if (o instanceof Map) {
            Map m = (Map) o;
            return m.isEmpty();
        } else {
            return true;
        }
    }

    public Date getNow() {
        return new Date();
    }

    public Templatable find(String path) {
        return ComponentUtils.find(page, Path.path(path));
    }

    public Component findComponent(Path path) {
        return ComponentUtils.findComponent(path, page);
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Component get(Object key) {
        String sKey = key.toString();
        Component c = page.getComponent(sKey, false);
        if (c != null) {
            return c;
        }
        ComponentValue cv = page.getValues().get(sKey);
        if (cv != null) {
            return cv;
        }
        return null;
    }

    @Override
    public Component put(String key, Component value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Component remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Component> m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Component> values() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entry<String, Component>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    String wrapWithIdentifier(String s, String name) {
        // oxygen xml doesnt send auth data for GETs of publi
//        if( RequestParams.current().getAuth() == null ) {
//            log.debug( "no logged in user, so not wrapping with markers");
//            return s;
//        }
        Templatable t = getTargetPage();
        String ct = t.getContentType(null);
        if (ct == null || ct.trim().length() == 0 || ct.equals("text/html")) { // ct==null means prolly template
            // interfere's with xml
            //return PARSER.addMarkers( s, name );
            return s;
        } else {
            //log.debug( "not ct: " + ct );
            return s;
        }
    }

    public BaseResource getPhysicalPage() {
        Templatable ct = getTargetPage();
        if (ct instanceof BaseResource) {
            return (BaseResource) ct;
        } else {
            return ct.getParentFolder();
        }
    }

    /**
     * Returns a list of classes identifying the current browser (ie user agent)
     *
     * The returned list has a toString function that will format the list
     * appropriately for a class
     *
     * @return
     */
    public List<String> getBrowserClasses() {
        BrowserList list = new BrowserList();
        // Eg: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13
        String browser = _(CurrentRequestService.class).request().getUserAgentHeader();
        String x =  _(CurrentRequestService.class).request().getParams().get("BrowserGrade");
        if( x != null ) {
            browser = x;
        }

        boolean isGradeA = true;
        String family;
        String version = null;
        if (browser.contains("MSIE")) {
            family = "ie";
            if (browser.contains("MSIE 6")) {
                version = "ie6";
                isGradeA = false;
            } else if (browser.contains("MSIE 7")) {
                version = "ie7";
            } else if (browser.contains("MSIE 8")) {
                version = "ie8";
            }
        } else if (browser.contains("Firefox")) {
            family = "firefox";
        } else if (browser.contains("Chrom")) {
            family = "chrome";
        } else if (browser.contains("Safari")) {
            family = "safari";
        } else {
            family = "unknownBrowser";
            // assume grade a because it might be standards compliant
        }
        list.add(family);
        if( isGradeA ) {
            list.add("grade-a");
        } else {
            list.add("grade-b");
        }
        if (version != null) {
            list.add(version);
        }
        return list;
    }

    public static class BrowserList extends ArrayList<String> {

        @Override
        public String toString() {
            String res = "";
            Iterator<String> it = iterator();
            while (it.hasNext()) {
                res += it.next();
                if (it.hasNext()) {
                    res += " ";
                }
            }
            return res;
        }
    }
}
