package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.web.*;
import com.bradmcevoy.xml.XmlHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

public class ComponentValue implements Component, Serializable, ValueHolder {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ComponentValue.class);
    private static final long serialVersionUID = 1L;
    public String name;
    public Object value;
    private List<OldValue> oldValues;
    private Addressable parent;
    private transient ThreadLocal<String> thValidationMessage = new ThreadLocal<String>();

    public ComponentValue(String name, Addressable container) {
        this.name = name;
        this.parent = container;
        if (parent instanceof ComponentValue) {
            throw new RuntimeException("Parent is a ComponentValue. This is probably a mistake: Name: " + name + " parent: " + parent.getName());
        }
        this.oldValues = new ArrayList<OldValue>();
    }

    public ComponentValue(Element el, Templatable container) {
        this.name = el.getAttributeValue("name");
        this.oldValues = new ArrayList<OldValue>();
        this.parent = container;
        if (parent instanceof ComponentValue) {
            throw new RuntimeException("Parent is a ComponentValue. This is probably a mistake: Name: " + name + " parent: " + parent.getName());
        }

        log.debug("created: " + this.name);
        String sVal = InitUtils.getValue(el);
        ComponentDef def = getDef(container);
        if (def == null) {
            log.warn("no container for CV " + name + ", cant' parse value so it will be a String!!!");
            this.value = sVal;
        } else {
            log.debug("parse value of CV");
            this.value = def.parseValue(this, container, sVal);
        }
    }

    public boolean isEmpty() {
        Object v = this.getValue();
        if (v == null) {
            return true;
        } else {
            if (v instanceof String) {
                String s = (String) v;
                return StringUtils.isBlank(s);
            } else {
                return false;
            }
        }
    }

    public List<OldValue> getOldValues() {
        if (oldValues == null) {
            return Collections.EMPTY_LIST;
        } else {
            return Collections.unmodifiableList(oldValues);
        }
    }

    public void setValidationMessage(String validationMessage) {
        log.debug("setValidationMessage: " + name + " -> " + validationMessage);
        if (thValidationMessage == null) {
            thValidationMessage = new ThreadLocal<String>();
        }
        thValidationMessage.set(validationMessage);
    }

    public String getValidationMessage() {
        if (thValidationMessage == null) {
            return null;
        }
        return thValidationMessage.get();
    }

    /**
     * placeholder, called after parent resource is saved
     * 
     * returns true if a change occured which must be saved
     */
    public boolean afterSave() {
        Object val = getValue();
        if (val == null) {
            return false;
        }
        if (val instanceof AfterSavable) {
            return ((AfterSavable) val).afterSave();
        } else {
            return false;
        }
    }

    @Override
    public void init(Addressable parent) {
        if (parent instanceof ComponentValue) {
            throw new RuntimeException("Parent is a ComponentValue. This is probably a mistake: Name: " + this.getName() + " parent: " + parent.getName());
        }
        this.parent = parent;
    }

    @Override
    public Addressable getContainer() {
        return parent;
    }

    @Override
    public boolean validate(RenderContext rc) {
        ComponentDef def = getDef(rc);
        if (def == null) {
            log.warn("No component definition for value: " + this.getName());
            return true;
        } else {
            return def.validate(this, rc);
        }
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = new Element("componentValue");
        el.addContent(e2);
        String clazzName = this.getClass().getName();
        log.debug("toXml: " + name);
        if (!clazzName.equals(ComponentValue.class.getName())) { // for brevity, only add class where not default
            e2.setAttribute("class", clazzName);
        }
        e2.setAttribute("name", name);
        int numOldVals = 0;
        if (oldValues != null) {
            numOldVals = oldValues.size();
        }
        InitUtils.set(e2, "numOldVals", numOldVals);
        String v = getFormattedValue((CommonTemplated) container);
        //        List l = formatContentToXmlList( v );
        //        e2.setContent( l );

        List content = XmlHelper.getContent(v);
        e2.setContent(content);
        Request req = HttpManager.request();
        String showOld;
        if (req != null && oldValues != null) {
            if (req.getParams() != null) {
                showOld = req.getParams().get("showold");
                if (showOld != null && showOld.length() > 0) {
                    for (OldValue ov : this.getOldValues()) {
                        Element elOld = new Element("oldval");
                        e2.addContent(elOld);
                        elOld.setAttribute("user", ov.user);
                        elOld.setAttribute("date", ov.getDateModified().toString());
                        v = getFormattedValue((CommonTemplated) container, ov.value);
                        content = XmlHelper.getContent(v);
                        elOld.setContent(content);
                    }
                }
            }
        }

        return e2;
    }

    @Override
    public String toString() {
        if (log.isTraceEnabled()) {
            log.trace("toString: " + this.getName() + " - " + this.value);
        }
        try {
            CommonTemplated ct = (CommonTemplated) this.getContainer();
            if (ct != null) {                
                RenderContext rc = new RenderContext(ct.getTemplate(), ct, null, false);
                String s = this.render(rc);
                return s;
            } else {
                Object o = this.getValue();
                return o == null ? "" : o.toString();
            }
        } catch (Exception e) {
            log.error("exception rendering componentvalue: " + this.getName(), e);
            return "ERR: " + this.getName() + ": " + e.getMessage();
        }
    }

    public String getEdit() {
        try {
            CommonTemplated ct = (CommonTemplated) this.getContainer();
            if (ct != null) {
                RenderContext rc = new RenderContext(ct.getTemplate(), ct, null, true);
                String s = this.renderEdit(rc);
                return s;
            } else {
                Object o = this.getValue();
                return o == null ? "" : o.toString();
            }
        } catch (Exception e) {
            log.error("exception rendering componentvalue: " + this.getName(), e);
            return "ERR: " + this.getName() + ": " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getValue() {
        if (value instanceof ComponentValue) {
            ComponentValue cvInner = (ComponentValue) value;
            log.warn("This ComponentValue somehow has a componentValue as its value??! my name: " + name + " other: " + cvInner.getName());
        }
        return value;
//        if( parent == null ) {
//            log.warn( "no parent set. Value might not be typed correctly");
//        }
//        // In some rare (and inexplicable) cases the value is not typed correctly
//        if( parent != null && parent instanceof Page ) {
//            return typedValue( (Page) parent);
//        } else {
//            return value;
//        }
    }

    public Object typedValue(Page page) {
        Object val = this.value;
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            return getDef(page).parseValue(this, page, (String) val);
        } else {
            return val;
        }
    }

    public void setValue(Object value) {
        if (log.isTraceEnabled()) {
            log.trace("setValue: name: " + name + " value: " + value);
        }
        // If we've been given another componentvalue just copy its value
        if (value instanceof ComponentValue) {
            ComponentValue other = (ComponentValue) value;
            value = other.getValue();
        }
        // note that this can cause an error sometimes, eg if the user name
        // has a space in it
//        if (this.value != null && !this.value.equals(value)) {
//            RequestParams cur = RequestParams.current();
//            String userName = null;
//            if (cur != null && cur.getAuth() != null) {
//                User user = (User) cur.getAuth().getTag();
//                if (user != null) {
//                    userName = user.getEmailAddress().toString();
//                }
//            }
//            OldValue old = new OldValue(value, new Date(), userName);
//            if (oldValues == null) {
//                oldValues = new ArrayList<OldValue>();
//            }
//            oldValues.add(old);
//        }
        this.value = value;
    }

    public ComponentDef getDef(RenderContext rc) {
        if (rc == null) {
            return null;
        }
        return getDef(rc.page);
//        Template templatePage = rc.template;
//        if( templatePage == null ) {
//            templatePage = (Template) rc.page; // if the page doesnt have a template, use itself as template
//        }
//        ComponentDef def = templatePage.getComponentDef(name);
//        if( def == null ) {
//            log.warn("did not find componentdef for: " + name);
//        }
//        return def;
    }

    /**
     * Locates this values definition from the given pages template
     * 
     * @param page
     * @return
     */
    public final ComponentDef getDef(Templatable page) {
        ITemplate templatePage = page.getTemplate();
        if (templatePage == null) {
            return null;
        }
        ComponentDef def = templatePage.getComponentDef(name);
        if (def == null) {
            log.warn("did not find componentdef for: " + name + " in template: " + templatePage.getName());
        }
        return def;
    }

    @Override
    public String render(RenderContext rc) {
        ComponentDef def = getDef(rc);
        if (def == null) {
            return "";
        }
        if (this.parent == null) {
            this.parent = rc.page;
            if (parent instanceof ComponentValue) {
                throw new RuntimeException("Parent is a ComponentValue. This is probably a mistake: Name: " + this.getName() + " parent: " + parent.getName());
            }

        }
        if (log.isTraceEnabled()) {
            log.trace("render CV: " + name + " ::: " + this.getValue());
        }
        return def.render(this, rc);
    }

    @Override
    public String renderEdit(RenderContext rc) {
        ComponentDef def = getDef(rc);
        if (def == null) {
            return "";
        }
        if (this.parent == null) {
            this.parent = rc.page;
            if (parent instanceof ComponentValue) {
                throw new RuntimeException("Parent is a ComponentValue. This is probably a mistake: Name: " + this.getName() + " parent: " + parent.getName());
            }

        }
        return def.renderEdit(this, rc);
    }

    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        init(rc.page);
        ComponentDef def = getDef(rc);
        if (def == null) {
            log.warn("Could not find definition for : " + this.name);  // this can happen when changing templates
        } else {
            def.onPreProcess(this, rc, parameters, files);
        }
    }

    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return null;
    }

    public String getFormattedValue(CommonTemplated container) {
        Object v = getValue();
        return getFormattedValue(container, v);
    }

    private String getFormattedValue(CommonTemplated container, Object v) {
        if (v instanceof String) {
            return v.toString();
        }
        ComponentDef def = getDef(container);
        if (def == null) {
            if (v == null) {
                return "";
            }
            return v.toString();
        }
        return def.formatValue(v);
    }

    public int getYear() {
        return Formatter.getInstance().getYear(getValue());
    }

    public int getMonth() {
        return Formatter.getInstance().getMonth(getValue());
    }

    public boolean gt(Object val2) {
        return Formatter.getInstance().gt(this.getValue(), val2);
    }

    public boolean lt(Object val1, Object val2) {
        return Formatter.getInstance().lt(this.getValue(), val2);
    }

    public boolean eq(Object val1, Object val2) {
        return Formatter.getInstance().eq(this.getValue(), val2);
    }

    public String getHtml() {
        return Formatter.getInstance().htmlEncode(this);
    }

    public static class OldValue implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Object value;
        private final Date dateModified;
        private final String user;

        public OldValue(Object value, Date dateModified, String user) {
            this.value = value;
            this.dateModified = dateModified;
            this.user = user;
        }

        public Date getDateModified() {
            return dateModified;
        }

        public String getUser() {
            return user;
        }

        public Object getValue() {
            return value;
        }
    }
}
