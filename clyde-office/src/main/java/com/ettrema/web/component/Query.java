
package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.BaseResourceList;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class Query extends CommonComponent implements Component, WrappableComponent {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Query.class);

    private static final long serialVersionUID = 1L;

    protected Addressable container;
    protected String name;
    protected int decimals;
    protected String sDateFormat;
    protected org.joda.time.format.DateTimeFormatter dateFormat;

    protected String from;
    protected String type;
    protected String where;
    protected String template;

    public Query(Addressable container, String name) {
        this.container = container;
        this.name = name;
    }

    public Query(Addressable container, Element el) {
        this.container = container;
        fromXml(el);
    }

    @Override
    public void init(Addressable container) {
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return container;
    }

    @Override
    public boolean validate(RenderContext rc) {
        return true;
    }

    public String render(RenderContext rc) {
        Object value = this.getValue(rc.page);
        VelocityContext vc = velocityContext(rc, value);
        String s = _render(template, vc);
        return s;
    }

    public String renderEdit(RenderContext rc) {
        return null;
    }

    public String getName() {
        return name;
    }

    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return null;
    }

    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {

    }

    public org.jdom.Element toXml(Addressable container, org.jdom.Element el) {
        Element e2 = new Element("component");
        el.addContent(e2);
        e2.setAttribute("name",name);
        e2.setAttribute("class",this.getClass().getName());
        InitUtils.set(e2, "decimals",decimals);
        InitUtils.setString(el, "dateformat", sDateFormat);
        InitUtils.addChild(e2,"from",from);
        InitUtils.addChild(e2,"type",type);
        InitUtils.addChild(e2,"where",where);
        InitUtils.addChild(e2,"template",template);
        return e2;

    }

    public void onPreProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {

    }

    public String onProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return null;
    }

    public String render(Addressable container, RenderContext rc) {
        return null;
    }

    public String renderEdit(Addressable container, RenderContext rc) {
        return null;
    }

    public boolean validate(Addressable container, RenderContext rc) {
        return true;
    }

    public BaseResourceList getValue(Addressable container) {
        log.debug("getValue");
        Templatable ct = (Templatable) container;
        Path path = Path.path(this.from);
        ct = ct.find(path);
        BaseResourceList list;
        if(ct instanceof Folder) {
            list = (BaseResourceList) ((Folder)ct).getChildren(type);
        } else {
            list = new BaseResourceList();
            list.add(ct);
        }
        list = list.getCalc().filter(this.where);
        return list;
    }

    public String getFormattedValue(Addressable container) {
        return null;
    }

    private void fromXml(Element el) {
        this.name = InitUtils.getValue(el, "name");
        this.decimals = InitUtils.getInt(el,"decimals");
        this.sDateFormat = InitUtils.getValue(el, "dateformat");
        if( this.sDateFormat != null ) {
            dateFormat = org.joda.time.format.DateTimeFormat.forPattern(sDateFormat);
        }
        this.from = InitUtils.getChild(el,"from");
        this.type = InitUtils.getChild(el,"type");
        this.where = InitUtils.getChild(el,"where");
        this.template = InitUtils.getChild(el,"template");
    }


}
