package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.ComponentDefMap;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate.DocType;
import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.Thumb;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.MetaHandler;
import com.bradmcevoy.web.code.meta.comp.BooleanDefHandler;
import com.bradmcevoy.web.code.meta.comp.ComponentDefHandler;
import com.bradmcevoy.web.code.meta.comp.DateDefHandler;
import com.bradmcevoy.web.code.meta.comp.EmailDefHandler;
import com.bradmcevoy.web.code.meta.comp.HtmlDefHandler;
import com.bradmcevoy.web.code.meta.comp.MultipleChoiceQaDefHandler;
import com.bradmcevoy.web.code.meta.comp.NumberDefHandler;
import com.bradmcevoy.web.code.meta.comp.PasswordDefHandler;
import com.bradmcevoy.web.code.meta.comp.ProcessDefHandler;
import com.bradmcevoy.web.code.meta.comp.RelationSelectDefHandler;
import com.bradmcevoy.web.code.meta.comp.SubPageHandler;
import com.bradmcevoy.web.code.meta.comp.TextDefHandler;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.InitUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class TemplateMetaHandler implements MetaHandler<Template> {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TemplateMetaHandler.class);
    public static final String ALIAS = "template";
    private final PageMetaHandler pageMetaHandler;
    private final Map<Class, String> mapOfAliasesByClass;
    private final Map<String, Class> mapOfClassesByAlias;
    private Map<Class, ComponentDefHandler> mapOfHandlers;
    private Map<String, ComponentDefHandler> mapOfHandlersByAlias;

    public TemplateMetaHandler(PageMetaHandler pageMetaHandler, Map<Class, String> mapOfAliases, SubPageHandler subPageHandler) {
        this.pageMetaHandler = pageMetaHandler;
        this.mapOfAliasesByClass = mapOfAliases;
        mapOfClassesByAlias = new HashMap();
        for (Entry<Class, String> entry : mapOfAliasesByClass.entrySet()) {
            mapOfClassesByAlias.put(entry.getValue(), entry.getKey());
        }
        mapOfHandlers = new LinkedHashMap<Class, ComponentDefHandler>();
        mapOfHandlersByAlias = new HashMap<String, ComponentDefHandler>();
        TextDefHandler textDefHandler = new TextDefHandler();
        NumberDefHandler numberDefHandler = new NumberDefHandler(textDefHandler);
        HtmlDefHandler htmlDefHandler = new HtmlDefHandler(textDefHandler);
        DateDefHandler dateDefHandler = new DateDefHandler(textDefHandler);
        EmailDefHandler emailDefHandler = new EmailDefHandler(textDefHandler);
        BooleanDefHandler booleanDefHandler = new BooleanDefHandler(textDefHandler);
        RelationSelectDefHandler relationSelectDefHandler = new RelationSelectDefHandler();
        MultipleChoiceQaDefHandler multipleChoiceQaDefHandler = new MultipleChoiceQaDefHandler();
        PasswordDefHandler passwordDefHandler = new PasswordDefHandler();
        ProcessDefHandler processDefHandler = new ProcessDefHandler(subPageHandler);

        add(processDefHandler);
        add(passwordDefHandler);
        add(multipleChoiceQaDefHandler);
        add(relationSelectDefHandler);
        add(booleanDefHandler);
        add(emailDefHandler);
        add(dateDefHandler);
        add(htmlDefHandler);
        add(numberDefHandler);
        add(textDefHandler);
    }

    private void add(ComponentDefHandler h) {
        mapOfHandlers.put(h.getDefClass(), h);
        mapOfHandlersByAlias.put(h.getAlias(), h);
    }

    public Class getInstanceType() {
        return Template.class;
    }

    public boolean supports(Resource r) {
        return r instanceof Template;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml(Template r) {
        Element elRoot = new Element(ALIAS, CodeMeta.NS);
        populateXml(elRoot, r);
        return elRoot;
    }

    public Template createFromXml(CollectionResource parent, Element d, String name) {
        Template page = new Template((Folder) parent, name);
        updateFromXml(page, d);
        return page;
    }

    private void populateXml(Element el, Template template) {
        String cn = template.getClassToCreate();
        if (!StringUtils.isEmpty(cn)) {
            try {
                Class c = Class.forName(cn);
                String s = mapOfAliasesByClass.get(c);
                if (s != null) {
                    cn = s;
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TemplateMetaHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        InitUtils.set(el, "instanceType", cn);
        InitUtils.set(el, "disableExport", template.isDisableExport());

        String dt = template.getDocType() == null ? null : template.getDocType().name();
        InitUtils.set(el, "docType", dt);

        initComponentDefs(el, template);

        populateThumbSpecs(el, template);

        pageMetaHandler.populateXml(el, template);

        JDomUtils.setChildText(el, "afterCreateScript", template.getAfterCreateScript(), CodeMeta.NS);

        JDomUtils.setChildText(el, "afterSaveScript", template.getAfterSaveScript(), CodeMeta.NS);
    }

    private void initComponentDefs(Element el, Template page) {
        ComponentDefMap defs = page.getComponentDefs();
        if (defs.isEmpty()) {
            return;
        }
        Element e2 = new Element("fields", CodeMeta.NS);
        el.addContent(e2);
        for (ComponentDef def : defs.values()) {
            ComponentDefHandler h = mapOfHandlers.get(def.getClass());
            if (h == null) {
                throw new RuntimeException("No ComponentDefHandler for: " + def.getClass());
            }
            Element elDef = h.toXml(def, page);
            e2.addContent(elDef);
        }
    }

    public void updateFromXml(Template template, Element el) {
        String instanceType = InitUtils.getValue(el, "instanceType");
        if (!StringUtils.isEmpty(instanceType)) {
            Class c = mapOfClassesByAlias.get(instanceType);
            if (c != null) {
                instanceType = c.getCanonicalName();
            } else {
                log.warn("--- Couldnt find class name for instance type: " + instanceType + " . Listing known types:");
                for (String alias : mapOfAliasesByClass.values()) {
                    log.warn("  alias: " + alias);
                }

            }
        }

        String dt = InitUtils.getValue(el, "docType");
        DocType docType = dt == null ? null : DocType.valueOf(dt);
        template.setDocType(docType);

        template.setDisableExport( InitUtils.getBoolean(el, "disableExport") );

        template.setAfterCreateScript(JDomUtils.valueOf(el, "afterCreateScript", CodeMeta.NS));
        String afterSave = JDomUtils.valueOf(el, "afterSaveScript", CodeMeta.NS);

        //System.out.println(afterSave);
        
        template.setAfterSaveScript(afterSave);

        template.setClassToCreate(instanceType);

        pageMetaHandler.updateFromXml(template, el);

        updateDefsFromXml(template, el);

        updateThumbSpecsFromXml(template, el);

        template.save();
    }

    private void updateDefsFromXml(Template res, Element el) {
        log.trace("updateDefsFromXml: " + el.getName());
        for (Element eAtt : JDomUtils.childrenOf(el, "fields", CodeMeta.NS)) {
            ComponentDefHandler h = mapOfHandlersByAlias.get(eAtt.getName());
            if (h == null) {
                throw new RuntimeException("Couldnt find component handler for element of type: " + eAtt.getName());
            }
            ComponentDef def = h.fromXml(res, eAtt);
            log.trace("add def: " + def.getName());
            res.getComponentDefs().add(def);
        }

    }

    private void populateThumbSpecs(Element elRoot, Template template) {
        List<Thumb> specs = Thumb.getThumbSpecs(template);
        if (specs != null && !specs.isEmpty()) {
            Element elThumbs = new Element("thumbs", CodeMeta.NS);
            elRoot.addContent(elThumbs);
            for (Thumb spec : specs) {
                Element elThumb = new Element("thumb", CodeMeta.NS);
                elThumb.setAttribute("id", spec.getSuffix());
                elThumb.setAttribute("h", spec.getHeight() + "");
                elThumb.setAttribute("w", spec.getWidth() + "");
                elThumbs.addContent(elThumb);
            }

        }

    }

    private void updateThumbSpecsFromXml(Template template, Element el) {
        List<Thumb> thumbs = new ArrayList<Thumb>();
        for (Element elThumb : JDomUtils.childrenOf(el, "thumbs", CodeMeta.NS)) {
            String suffix = elThumb.getAttributeValue("id");
            int height = InitUtils.getInt(elThumb, "h");
            int width = InitUtils.getInt(elThumb, "w");
            Thumb spec = new Thumb(suffix, width, height);
            thumbs.add(spec);
        }
        Thumb.setThumbSpecs(template, thumbs);
    }
}
