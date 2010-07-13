package com.bradmcevoy.web;

/**
 *
 * @author brad
 */
public class TemplateManagerImpl implements TemplateManager{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TemplateManagerImpl.class );

    public ITemplate lookup( String templateName, Web web ) {
        if( templateName == null ) return null;
        Folder templates = web.getTemplates();
//        log.debug( "templates: " + templates.getPath());
        if( templates == null ) {
            throw new NullPointerException( "No templates folder for web: " + web.getName() );
        }
        if( templateName.equals( "root" ) ) {
            return Root.getInstance( templates );
        }
        if( templateName.equals( "rootTemplate.html" ) ) {
            return new RootTemplate( templates );
        } else {
            Template template = null;
            BaseResource res = templates.childRes( templateName );  // note: do not call .child(..) here since that will check for components, and possibly result in infinite loop
            if( res != null && !( res instanceof Template ) ) {
                throw new RuntimeException( "not a Template: " + res.getPath() + " is a: " + res.getClass() );
            }
            template = (Template) res;
            if( template == null ) {
                Web parentWeb = web.getParentWeb();
                if( parentWeb != null && parentWeb != web ) {
                    ITemplate t = lookup( templateName, parentWeb );
                    if( t != null ) {
                        return new WrappedTemplate( t, web );
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                if( template.getWeb() != web ) {
                    return new WrappedTemplate( template, web );
                } else {
                    return template;
                }
            }
        }
    }

}
