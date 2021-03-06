package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.Folder;
import com.ettrema.web.TemplateSpecs;
import com.ettrema.web.Thumb;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.templates.TemplateMapping;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class FolderMetaHandler implements MetaHandler<Folder> {

    public static final String ALIAS = "folder";
    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public FolderMetaHandler(BaseResourceMetaHandler baseResourceMetaHandler) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return Folder.class;
    }

    @Override
    public boolean supports(Resource r) {
        return r instanceof Folder;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public Element toXml(Folder r) {
        Element elRoot = new Element(ALIAS, CodeMeta.NS);
        populateXml(elRoot, r);
        return elRoot;
    }

    @Override
    public void applyOverrideFromXml(Folder r, Element el) {
        baseResourceMetaHandler.applyOverrideFromXml(r, el);
        r.save();
    }

    
    @Override
    public Folder createFromXml(CollectionResource parent, Element d, String name) {
        Folder f = new Folder((Folder) parent, name);
        updateFromXml(f, d);
        return f;
    }

    public void populateXml(Element el, Folder folder) {
        InitUtils.set(el, "secureRead", folder.isSecureRead2());
        InitUtils.set(el, "versioningEnabled", folder.isVersioningEnabled());
        TemplateSpecs templateSpecs = folder.getTemplateSpecs();
        if (templateSpecs == null) {
            templateSpecs = new TemplateSpecs("");
        }
        InitUtils.set(el, "allowedTemplates", templateSpecs.format());
        baseResourceMetaHandler.populateXml(el, folder, true);
        populateThumbSpecs(el, folder);
        populateTemplateMappings(el, folder);

    }

    @Override
    public void updateFromXml(Folder folder, Element d) {
        _updateFromXml(folder, d);
        folder.save();
    }

    public void _updateFromXml(Folder folder, Element el) {
        folder.setSecureRead(InitUtils.getBoolean(el, "secureRead"));
        folder.setVersioningEnabled(InitUtils.getBoolean(el, "versioningEnabled"));
        String sAllowedTemplates = el.getAttributeValue("allowedTemplates");
        TemplateSpecs templateSpecs = TemplateSpecs.parse(sAllowedTemplates);
        folder.setTemplateSpecs(templateSpecs);
        updateThumbSpecsFromXml(folder, el);
        updateTemplateMappings(folder, el);

        baseResourceMetaHandler.updateFromXml(folder, el, false);

    }

    private void populateThumbSpecs(Element elRoot, Folder folder) {
        List<Thumb> specs = Thumb.getThumbSpecs(folder);
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

    private void updateThumbSpecsFromXml(Folder folder, Element el) {
        List<Thumb> thumbs = new ArrayList<>();
        for (Element elThumb : JDomUtils.childrenOf(el, "thumbs", CodeMeta.NS)) {
            String suffix = elThumb.getAttributeValue("id");
            int height = InitUtils.getInt(elThumb, "h");
            int width = InitUtils.getInt(elThumb, "w");
            Thumb spec = new Thumb(suffix, width, height);
            thumbs.add(spec);
        }
        Thumb.setThumbSpecs(folder, thumbs);
    }

    private void populateTemplateMappings(Element el, Folder folder) {
        if( folder.getTemplateMappings() == null || folder.getTemplateMappings().isEmpty()) {
            return ;
        }
        Element elMappings = new Element("templateMappings", CodeMeta.NS);
        el.addContent(elMappings);
        for( TemplateMapping tm : folder.getTemplateMappings()) {
            Element elTm = new Element("mapping", CodeMeta.NS);
            elMappings.addContent(elTm);
            elTm.setAttribute("mime", tm.getMimeType());
            elTm.setAttribute("template", tm.getTemplateName());
        }
    }

    private void updateTemplateMappings(Folder folder, Element el) {
        List<TemplateMapping> mappings = null;
        for (Element elTm : JDomUtils.childrenOf(el, "templateMappings", CodeMeta.NS)) {
            String mime = elTm.getAttributeValue("mime");
            String template = elTm.getAttributeValue("template");
            TemplateMapping tm = new TemplateMapping(mime, template);
            if( mappings == null ) {
                mappings = new ArrayList<>();
            }
            mappings.add(tm);
        }
        folder.setTemplateMappings(mappings);
    }
}
