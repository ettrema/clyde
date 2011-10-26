package com.bradmcevoy.web;

/**
 *
 * @author brad
 */
public class TemplateManagerImpl implements TemplateManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TemplateManagerImpl.class);

    @Override
    public ITemplate lookup(String templateName, Folder folder) {
        if (templateName == null) {
            return null;
        }
        Web web = folder.getWeb();
        Folder templates = web.getTemplates();
        log.debug("lookup: " + templateName + " templates: " + templates.getPath());
        if (templates == null) {
            throw new NullPointerException("No templates folder for web: " + web.getName());
        }
        if (templateName.equals("root")) {
            return Root.getInstance(templates);
        }

        Template template = null;
        BaseResource res = templates.childRes(templateName);  // note: do not call .child(..) here since that will check for components, and possibly result in infinite loop
        if (res != null && !(res instanceof Template)) {
            log.warn("not a Template: " + res.getPath() + " is a: " + res.getClass() + " ---------");
            return null;
        }
        template = (Template) res;
        if (template == null) {
            log.debug("..not found1");
            Web parentWeb = web.getParentWeb();
            if (parentWeb != null && parentWeb != web) {
                ITemplate t = lookup(templateName, parentWeb);
                if (t != null) {
                    log.debug("got wrapped template");
                    return new WrappedTemplate(t, web);
                } else {
                    log.debug("..not found3");
                    return null;
                }
            } else {
                log.debug("..not found4");
                return null;
            }
        } else {
            if (template.getWeb() != web) {
                return new WrappedTemplate(template, web);
            } else {
                return template;
            }
        }

    }
}
