package com.ettrema.web.code.meta.comp;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.ITemplate;
import com.ettrema.web.Templatable;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.InitUtils;
import com.bradmcevoy.xml.XmlHelper;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.component.RelationSelectDef;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author brad
 */
public class DefaultValueHandler implements ValueHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultValueHandler.class);
    public static Namespace NS_HTML_DEFAULT = Namespace.getNamespace("http://www.w3.org/1999/xhtml");

    @Override
    public Class getComponentValueClass() {
        return ComponentValue.class;
    }

    @Override
    public Element toXml(ComponentValue cv, CommonTemplated container) {
        Element el = new Element(getAlias(), CodeMeta.NS);
        populateXml(el, cv, container);
        return el;
    }

    public void populateXml(Element e2, ComponentValue cv, CommonTemplated container) {
        e2.setAttribute("name", cv.getName());
        String v = cv.getFormattedValue(container);
        List content = XmlHelper.getContent(v);
        e2.setContent(content);
    }

    @Override
    public String getAlias() {
        return "value";
    }

    @Override
    public ComponentValue fromXml(CommonTemplated res, Element eAtt) {
        String name = eAtt.getAttributeValue("name");
        ComponentValue cv = new ComponentValue(name, res);
        fromXml(eAtt, res, cv);
        return cv;
    }

    public void fromXml(Element eAtt, CommonTemplated res, ComponentValue cv) {
        //String sVal = InitUtils.getValue( eAtt );
        ComponentDef def = getDef(res, cv.getName());
        String sVal = InitUtils.getValue(eAtt);
        if (def == null) {
//            throw new RuntimeException( "No definition for : " + cv.getName() + " in template: " + res.getTemplateName());
            log.warn("No definition for : " + cv.getName() + " in template: " + res.getTemplateName() + " The value will be represented as a string, which might cause errors if its intended to be a structured tye");
            cv.setValue(sVal);
        } else {
            LogUtils.trace(log, "fromXml: using def:", def.getName());
            cv.setValue(def.parseValue(cv, res, eAtt));
            def.changedValue(cv);
        }
    }

    public ComponentDef getDef(Templatable page, String name) {
        ITemplate templatePage = page.getTemplate();
        if (templatePage == null) {
            log.warn("No template for: " + page.getName() + " template name: " + page.getTemplateName());
            return null;
        }
        ComponentDef def = templatePage.getComponentDef(name);
        return def;
    }
}
