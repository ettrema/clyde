package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.ComponentMap;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Web;
import java.util.Map;
import org.jdom.Element;

public class CreateWebCommand extends CreateCommand {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CreateWebCommand.class);
    
    private static final long serialVersionUID = 1L;

    protected ThemeSelect themeSelect;
    
    public CreateWebCommand(Addressable container, String name) {
        super(container, name);
        themeSelect = new ThemeSelect(this, "themeSelect");
    }

    public CreateWebCommand(Addressable container, Element el) {
        super(container, el);
    }

    public ThemeSelect getThemeSelect() {
        if( themeSelect == null ) {
            themeSelect = new ThemeSelect( container, "themeSelect");
            themeSelect.setRequestScope( true );
        }
        return themeSelect;
    }

    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        super.onPreProcess(rc, parameters, files);
        getThemeSelect().onPreProcess(rc, parameters, files);
    }

    @Override
    public boolean validate(RenderContext rc) {
        boolean b = super.validate(rc);
        b = b & getThemeSelect().validate(rc);
        return b;
    }


    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = super.toXml(container, el);
        if( themeSelect != null ) {
            themeSelect.toXml(container, e2);
        }
        return e2;
    }


    @Override
    protected void fromXml(ComponentMap map) {
        super.fromXml(map);
        Component c = map.get("themeSelect");
        if( c == null ) {
            themeSelect = getThemeSelect();
        } else {
            themeSelect = (ThemeSelect) c;
        }
    }

    @Override
    protected void afterCreate(BaseResource res, RenderContext rc) {
        if (res instanceof Web) {
            Web newWeb = (Web) res;
            newWeb.getThemeSelect().setValue( getThemeSelect().getValue());
            newWeb.save();
            // dont copy templates. instead, use theme
            //copyTemplates(newWeb);
        } else {
            throw new RuntimeException("expect resource of type web, not: " + res.getClass());
        }
        super.afterCreate( res, rc );
    }

//    protected void copyTemplates(Web newWeb) {
//        Folder newTemplates = newWeb.getTemplates();
//        Folder parentTemplates = parentTemplates(newWeb);
//        for (Resource parentRes : parentTemplates.getChildren()) {
//            if (parentRes instanceof BaseResource) {
//                BaseResource f = (BaseResource) parentRes;
//                f._copyTo(newTemplates);
//            }
//        }
//
//        Template indexTemplate = newWeb.getTemplate("normal");
//        BaseResource index = indexTemplate.createPageFromTemplate(newWeb, "index.html");
//        index.save();
//    }
//
//    protected Folder parentTemplates(Web newWeb) {
//        Web parentWeb = newWeb.getParentWeb();
//        Folder parentTemplates = parentWeb.getTemplates();
//        return parentTemplates;
//    }
}
