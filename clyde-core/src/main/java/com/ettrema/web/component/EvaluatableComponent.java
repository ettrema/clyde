package com.ettrema.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Formatter;
import com.ettrema.web.RenderContext;
import com.ettrema.web.eval.EvalUtils;
import com.ettrema.web.eval.Evaluatable;
import java.io.Serializable;
import java.util.Map;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author brad
 */
public class EvaluatableComponent extends CommonComponent implements Serializable{

    public static final Namespace NS = Namespace.getNamespace( "c", "http://clyde.ettrema.com/ns/core" );

    private static final long serialVersionUID = 1L;

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
        evaluatable = EvalUtils.getEvalDirect(el, NS, container);
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

	@Override
    public String render(RenderContext rc) {
        Object o = eval(rc);
        return Formatter.getInstance().toString(o);
    }

    public String render(CommonTemplated page) {
        RenderContext rc = new RenderContext(page.getTemplate(), page, null, false);
        Object o = eval(rc);
        return Formatter.getInstance().toString(o);
    }

	@Override
    public String renderEdit(RenderContext rc) {
        Object o = eval(rc);
        return Formatter.getInstance().toString(o);
    }

	@Override
    public String getName() {
        return name;
    }

	@Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
        Object result = evaluatable.evaluate(rc, container);
        if( result == null ) {
            return "";
        } else {
            return result.toString();
        }
    }

	@Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {

    }

	@Override
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

    public Evaluatable getEvaluatable() {
        return evaluatable;
    }

    
}
