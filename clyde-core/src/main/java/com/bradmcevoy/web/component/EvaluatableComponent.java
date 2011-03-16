package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.eval.EvalUtils;
import com.bradmcevoy.web.eval.Evaluatable;
import java.util.Map;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author brad
 */
public class EvaluatableComponent extends CommonComponent{

    public static final Namespace NS = Namespace.getNamespace( "c", "http://clyde.ettrema.com/ns/core" );

    private Addressable container;
    private String name;
    private Evaluatable evaluatable;

    public EvaluatableComponent(Addressable container, String name) {
        this.container = container;
        this.name = name;
    }

    public EvaluatableComponent(Addressable container, Element el) {
        this.container = container;
        name = el.getAttributeValue("name");
        evaluatable = EvalUtils.getEvalDirect(el, NS);
    }

    public void init(Addressable container) {
        this.container = container;
    }

    public Addressable getContainer() {
        return container;
    }

    public boolean validate(RenderContext rc) {
        return true;
    }

    public String render(RenderContext rc) {
        Object o = eval(rc);
        return Formatter.getInstance().toString(o);
    }

    public String renderEdit(RenderContext rc) {
        Object o = eval(rc);
        return Formatter.getInstance().toString(o);
    }

    public String getName() {
        return name;
    }

    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
        Object result = evaluatable.evaluate(rc, container);
        if( result == null ) {
            return "";
        } else {
            return result.toString();
        }
    }

    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {

    }

    public Element toXml(Addressable container, Element el) {
        Element e2 = new Element("component");
        el.addContent(e2);
        populateXml(e2);
        return e2;
    }
    public void populateXml(Element e2) {
        e2.setAttribute("class", getClass().getName());
        _populateXml(e2);
    }
    public void _populateXml(Element e2) {
        e2.setAttribute("name", name);
        EvalUtils.setEvalDirect(e2, evaluatable, NS);
    }

    public Object eval(RenderContext rc) {
        //Object result = evaluatable.evaluate(rc, container);
        Object result = EvalUtils.eval(evaluatable, rc, rc.getTargetPage());
        return result;
    }
}
